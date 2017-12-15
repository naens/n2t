all: VMTranslator

Parser:
	fpc Parser.pas

CodeWriter: Parser
	fpc CodeWriter.pas

VMTranslator: Parser CodeWriter
	fpc VMTranslator.pas
	rm *.o *.ppu

clean:
	rm -f VMTranslator *.o *.ppu
