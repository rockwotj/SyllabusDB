mvn:
	bazel run @unpinned_maven//:pin

fmt:
    java -jar ./tools/google-java-format.jar --replace `find . -name "*.java"`
    buildifier -r .

book:
	cd ./docs && ../tools/mdbook/{{os()}}/mdbook serve

