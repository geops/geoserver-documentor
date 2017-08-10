#!/usr/bin/env python
# coding: utf8

import logging
import argparse
import ConfigParser
import sys
import shutil
import os.path
import os
import codecs
from lxml import etree

from rstlib import (
    escape,
    make_filename,
    make_heading,
    make_layer_xref_link,
    make_layer_xref_target,
    make_store_xref_link,
    make_store_xref_target,
    make_sub2_heading,
    make_sub_heading,
    make_table_xref_link,
    make_table_xref_target,
    write_ln,
    write_rst_code,
    write_rst_table,
)

# gsconfig module
from geoserver.catalog import Catalog

logging.basicConfig(level=logging.DEBUG)
log = logging.getLogger('documentor_client')


def parse_config(configfile):
    log.info("Reading config from {0}".format(configfile))
    config = ConfigParser.ConfigParser()
    config.read([configfile])
    return config


def fail(msg):
    log.error(msg)
    sys.exit(1)


def fetch_documentation(catalog):
    full_doc_url = "{0}/documentor/complete.xml".format(catalog.service_url)
    response, content = catalog.http.request(full_doc_url, "GET")
    if response.status != 200:
        fail("Geoserver relied with HTTP status {0}".format(response.status))
    content_type = response.get("content-type", "").lower()
    if not content_type.startswith("application/xml"):
        fail("Expected to get XML from Geoserver, but got {0}".format(response.get(
            "content-type", "???"
        )))
    root = etree.fromstring(content)

    def parse_element(e):
        """hacky parser to print geoservers xml output in the form of python data structures"""
        eres = None
        l_child_names = len(set(child.tag for child in e))

        # tags which should be a dict, but may contain only one element
        if e.tag.startswith("de.geops.geoserver.documentor.info.") \
                or l_child_names > 1 \
                or e.tag in ('sqlView', 'featureType', 'defaultStyle'):
            # this will drop tags if a element contains multiple child tags of the same name
            # as well as other tags
            eres = {}
        elif l_child_names == 1:
            eres = []
        elif l_child_names == 0:
            eres = e.text

        for child in e:
            if type(eres) == dict:
                eres[child.tag] = parse_element(child)
            elif type(eres) == list:
                eres.append(parse_element(child))
            else:
                log.debug(child.tag)
                raise Exception("not supposed to happen")
        return eres
    return parse_element(root)


def sort_related_tables(tables):
    def get_sortkey(rt):
        return rt.get("tableSchema")+"."+rt.get("tableName")
    unique_tables = dict()
    for t in tables:
        unique_tables[get_sortkey(t)] = t
    return sorted(unique_tables.values(), key=get_sortkey)


def write_rst(documentation, output_dir):
    if not os.path.isdir(output_dir):
        os.makedirs(output_dir)
    shutil.rmtree(output_dir)
    os.makedirs(output_dir)
    log.info("Writing output to {0}".format(output_dir))
    for workspace in documentation.get('workspaces', []):
        workspace_ident = workspace.get("name", "_global_")
        outfilename = os.path.join(output_dir, 'workspaces', make_filename(workspace_ident))
        write_workspace_rst(workspace, workspace_ident, outfilename)


def write_workspace_rst(workspace, workspace_ident, outfilename):
    ws_dir = os.path.join(os.path.dirname(outfilename), workspace_ident)

    def make_index(idx_dir, idx, idx_title):
        with codecs.open(os.path.join(idx_dir, idx+'.rst'), "w", "utf8") as list_out:
            list_out.write(make_heading(idx_title))
            write_ln(list_out, ".. toctree::")
            write_ln(list_out, "   :maxdepth: 1")
            write_ln(list_out, "   :glob:")
            write_ln(list_out, "   ")
            write_ln(list_out, "   "+idx+"/*")

    has_layers = len(workspace.get("layers", [])) > 0
    has_layergroups = len(workspace.get("layerGroups", [])) > 0

    layerdir = os.path.join(ws_dir, "layers")
    datasourcedir = os.path.join(ws_dir, "datasources")
    layergroupsdir = os.path.join(ws_dir, "layergroups")
    if has_layers:
        os.makedirs(layerdir)
        make_index(ws_dir, "layers", "Layer")

        os.makedirs(datasourcedir)
        make_index(ws_dir, 'datasources', "Datenquellen")

    if has_layergroups:
        os.makedirs(layergroupsdir)
        make_index(ws_dir, 'layergroups', "Layergruppen")

    log.info("Writing {0}".format(outfilename))
    with codecs.open(outfilename, "w", "utf8") as out:
        title = "Workspace {0}".format(workspace.get("name", "[Global]"))
        out.write(make_heading(title))
        write_ln(out, ".. toctree::")
        write_ln(out, "   :maxdepth: 2")
        write_ln(out, "   ")

        if has_layers:
            write_ln(out, "   " + workspace_ident + "/layers.rst")
            write_ln(out, "   " + workspace_ident + "/datasources.rst")
            for layer in workspace.get("layers", []):
                layerfilename = os.path.join(layerdir, make_filename(layer["name"]))
                write_layer_rst(layer, layerfilename)

        if has_layergroups:
            write_ln(out, "   " + workspace_ident + "/layergroups.rst")
            for layergroup in workspace.get("layerGroups", []):
                layergroupfilename = os.path.join(layergroupsdir, make_filename(layergroup["name"]))
                write_layergroup_rst(layergroup, layergroupfilename)

    # collect datasources and their tables, ...
    ds_dict = {}
    ds_contents_dict = {}
    for layer in workspace.get("layers", []):
        store_name = layer["store"]["name"]
        if store_name not in ds_dict:
            ds_dict[store_name] = layer["store"]
            ds_contents_dict[store_name] = {}
        additional_data = ds_contents_dict[store_name]
        if layer["store"]["type"] == 'PostGIS':
            if "relatedTables" not in additional_data:
                additional_data["relatedTables"] = []
            # TODO: remove duplicates
            for rt in layer.get("featureType", {}).get("relatedTables", []):
                additional_data["relatedTables"].append(rt)
        ds_contents_dict[store_name] = additional_data

    for store_name, store in ds_dict.iteritems():
        storefilename = os.path.join(datasourcedir, make_filename(store_name))
        write_store_rst(store, workspace["name"], ds_contents_dict.get(store_name, {}),
                        storefilename)


def write_store_rst(store, workspaceName, additional_data, outfilename):
    with codecs.open(outfilename, "w", "utf8") as out:
        out.write(make_store_xref_target(workspaceName, store["name"]))
        out.write("\n\n")
        title = "Datenquelle {0}".format(store["name"])
        out.write(make_heading(title))
        write_ln(out, "* **Typ**: "+store.get("type", "?"))

        extended_infos = store.get("info", [])
        if extended_infos:
            for ei in extended_infos:
                write_ln(out, "* **"+escape(ei[0])+"**: "+escape(ei[1]))

        if "relatedTables" in additional_data:
            for relatedTable in sort_related_tables(additional_data["relatedTables"]):
                heading = ""
                table_type = "Unbekannt"
                if relatedTable.get("type", "") == "TABLE":
                    table_type = "Tabelle"
                elif relatedTable.get("type", "") == "VIEW":
                    table_type = "View"
                table_name = relatedTable.get("tableName")
                table_schema = relatedTable.get("tableSchema")
                if table_schema:
                    heading += table_schema+"."
                heading += table_name
                write_ln(out, make_table_xref_target(workspaceName, store["name"], table_schema,
                                                     table_name))
                out.write(make_sub2_heading(heading))

                out.write("**Typ**: "+table_type+"\n")
                write_ln(out, "")

                comment = relatedTable.get("comment")
                if comment:
                    write_ln(out, "**Beschreibung**")
                    write_ln(out, "")
                    write_ln(out, escape(comment))
                    write_ln(out, "")

                definition = relatedTable.get("definition")
                if definition:
                    write_ln(out, "**Definition**")
                    write_rst_code(out, definition, syntax="postgres", wrap=True)
                    write_ln(out, "")

                if "columns" in relatedTable:
                    write_ln(out, "**Spalten**")
                    write_ln(out, "")
                    headers = ["Name", "Datentyp", "Kommentar"]
                    data = []
                    for p in relatedTable.get("columns", []):
                        data.append([
                                escape(p.get("name")),
                                escape(p.get("type")),
                                escape(p.get("comment"))])
                    write_rst_table(
                        out,
                        sorted(data, key=lambda x: x[0]),
                        headers=headers
                    )
                write_ln(out, "")


def write_layergroup_rst(layergroup, outfilename):
    log.info("Writing {0}".format(outfilename))
    with codecs.open(outfilename, "w", "utf8") as out:
        pretty_name = "{0}:{1}".format(layergroup.get("workspaceName", ""), layergroup["name"])

        write_ln(out, make_layer_xref_target(layergroup.get("workspaceName"), layergroup["name"]))
        out.write(make_heading("Layergruppe "+pretty_name))

        out.write(make_sub_heading("Allgemeine Informationen"))
        out.write("\n")
        if "description" in layergroup:
            out.write(escape(layergroup["description"]))
            out.write("\n")
            out.write("\n")

        write_ln(out, "* *Titel*: "+escape(layergroup.get("title", "")))
        write_ln(out, "* *Workspace*: "+layergroup.get("workspaceName", ""))
        write_ln(out, "\n\n")

        out.write(make_sub_heading("Layer"))
        out.write("\n")
        for layer in layergroup.get("layers", []):
            out.write("* "+make_layer_xref_link(layer.get("workspaceName"), layer.get("name"))+"\n")


def write_layer_rst(layer, outfilename):
    log.info("Writing {0}".format(outfilename))
    with codecs.open(outfilename, "w", "utf8") as out:
        pretty_name = "{0}:{1}".format(layer.get("workspaceName"), layer["name"])

        index_entries = [pretty_name]
        index_see_entries = layer.get("keywords", [])

        write_ln(out, ".. index::")
        write_ln(out, "   single: "+"; ".join(index_entries))
        write_ln(out, "   see: "+"; ".join(index_see_entries))

        write_ln(out, make_layer_xref_target(layer.get("workspaceName"), layer["name"]))
        out.write(make_heading("Layer "+pretty_name))

        out.write(make_sub_heading("Allgemeine Informationen"))
        out.write("\n")
        if "description" in layer:
            out.write(escape(layer["description"]))
            out.write("\n")
            out.write("\n")

        write_ln(out, "* *Nativer Name*: "+escape(layer.get("nativeName", "")))
        write_ln(out, "* *Titel*: "+escape(layer.get("title", "")))
        write_ln(out, "* *Layertyp*: "+escape(layer.get("type", "?")))
        write_ln(out, "* *Workspace*: "+layer.get("workspaceName", ""))
        write_ln(out, "* *Aktiv*: " +
                 ("Ja" if layer.get("isEnabled", 'False').lower() == "true" else "Nein"))
        write_ln(out, "* *Im GetCapabilities gelistet*: " +
                 ("Ja" if layer.get("isAdvertized", 'False').lower() == "true" else "Nein"))
        write_ln(out, "\n\n")

        feature_type = layer.get("featureType")
        store = layer.get("store")

        if layer.get("type", "") == "vector":
            if feature_type:
                out.write(make_sub_heading("Attribute"))

                headers = ["Attribut Name", "Datentyp"]
                data = []
                for p in feature_type.get("properties", []):
                    data.append([
                            p.get("name"),
                            p.get("type")])

                write_rst_table(
                    out,
                    sorted(data, key=lambda x: x[0]),
                    headers=headers
                )
                write_ln(out, "\n\n")

        def make_style_name(style):
            if style:
                if "workspaceName" in style:
                    return "{0}:{1}".format(style["workspaceName"], style.get("name", "?"))
                else:
                    return "{0}".format(style.get("name", "?"))
            return ""

        out.write(make_sub_heading("Zeichenstil"))
        write_ln(out, "* Standardstil: "+make_style_name(layer.get("defaultStyle", {})))
        write_ln(out, "\n\n")
        styles = layer.get("styles", {})
        if styles:
            write_ln(out, u"**Alternative Stile**\n")
            for style in styles:
                write_ln(out, "* "+make_style_name(style))
            write_ln(out, "")

        if store:
            out.write(make_sub_heading("Datenquelle"))
            write_ln(out, "* Name: "+store.get("name", "?"))
            write_ln(out, "* Typ: "+store.get("type", "?"))
            write_ln(out, "\n")

            out.write(u"Weiterführende Informationen finden sich unter  " +
                      make_store_xref_link(layer.get("workspaceName"), store.get("name"))+".\n")

            write_ln(out, "\n\n")

            if feature_type:
                sql_view = feature_type.get("sqlView")
                if sql_view:
                    out.write(make_sub2_heading("SQL View"))
                    write_ln(out, "**Definition**")
                    write_rst_code(out, sql_view.get("sqlQuery"), syntax="postgres")
                    write_ln(out, "\n")

                    write_ln(out, "**Parameter**")
                    write_ln(out, "")
                    if "parameters" in sql_view:
                        headers = ["Name", "Standartwert", "Validator"]
                        data = []
                        for p in sql_view["parameters"]:
                            data.append([
                                    escape(p.get("name")),
                                    escape(p.get("defaultValue")),
                                    escape(p.get("validator"))])
                        write_rst_table(
                            out,
                            sorted(data, key=lambda x: x[0]),
                            headers=headers
                        )
                    else:
                        write_ln(out, u"Keine Parametrisierung des Views möglich.")
                        write_ln(out, u"")

                out.write(make_sub2_heading("Tabellen"))
                maintables = []
                othertables = []
                for relatedTable in sort_related_tables(feature_type.get("relatedTables", [])):
                    if relatedTable.get("isMainTable", 'false').lower() == 'true':
                        maintables.append(relatedTable)
                    else:
                        othertables.append(relatedTable)

                def write_tables(tables):
                    write_ln(out, "")
                    for table in tables:
                        table_name = table.get("tableName")
                        table_schema = table.get("tableSchema")
                    out.write(
                            '* ' +
                            make_table_xref_link(
                                layer.get("workspaceName", ""),
                                store["name"],
                                table_schema,
                                table_name
                            ) +
                            "\n")

                if maintables:
                    write_ln(out, "**Haupttabelle**")
                    write_tables(maintables)
                    write_ln(out, "")

                if othertables:
                    write_ln(out, "**Involvierte Relationen**")
                    write_tables(othertables)
                    write_ln(out, "")

        doc_errors = layer.get("documentationErrors", [])
        if doc_errors:
            out.write(make_sub_heading("Fehler bei der Dokumentationserstellung"))
            for doc_error in doc_errors:
                write_ln(out, "* "+escape(doc_error))


def run():
    parser = argparse.ArgumentParser()
    parser.add_argument("configfile")
    args = parser.parse_args()
    config = parse_config(args.configfile)

    geoserver_url = config.get("geoserver", "url").rstrip("/ ")
    if not geoserver_url.endswith("rest"):
        geoserver_url += "/rest/"

    log.info("Using geoserver at {0}".format(geoserver_url))
    catalog = Catalog(
        geoserver_url,
        username=config.get("geoserver", "user"),
        password=config.get("geoserver", "password").strip(" \"'")
    )
    write_rst(fetch_documentation(catalog), config.get("output", "directory"))


if __name__ == "__main__":
    run()
