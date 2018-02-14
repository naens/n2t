JACK := $(shell command -v JackCompiler 2> /dev/null)

all:
ifndef JACK
	$(error JackCompiler not found)
endif
	JackCompiler

clean:
	rm -f *.vm *~
