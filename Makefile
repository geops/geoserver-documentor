MVN=mvn
SHELL=bash
M2_REPO=~/.m2
GITVERSION_FILE=src/main/resources/geoserver-documentor.gitversion

build: git-version readme
	rm -f target/*.jar target/*.zip
	$(MVN) -Dmaven.test.skip=true package
	cd target && zip all-jars.zip *.jar

package: build

test:
	$(MVN) test

readme:
	cp README.md src/main/resources/README.documentor.md

git-version:
	./print-git-commit-hash.sh >$(GITVERSION_FILE)
	@# update the copy of the file cached in mavens build directory
	([ -d target/classes/ ] && cp $(GITVERSION_FILE) target/classes/) || true

clean:
	rm -f $(GITVERSION_FILE) src/main/resources/README.documentor.md
	$(MVN) clean
