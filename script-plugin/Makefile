all: install

clean:
	rm -rf build

build:
	mkdir -p build/libs build/zip-content/script-plugin
	cp -r contents resources plugin.yaml build/zip-content/script-plugin
	cd build/zip-content; zip -r script-plugin.zip *
	mv build/zip-content/script-plugin.zip build/libs

