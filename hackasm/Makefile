all: addenv
	@. ../fun.sh && ../runsub.sh mk
	@echo "cpm basasm \$$1" > basasm
	@echo "cpm hackasm \$$1" > hackasm
	@. ../fun.sh && deltemp
	@. ../fun.sh && delenv
	@chmod +x basasm hackasm

basasm: addenv
	@. ../fun.sh && ../runsub.sh mkb
	@echo "cpm basasm \$$1" > basasm
	@. ../fun.sh && deltemp
	@. ../fun.sh && delenv
	@chmod +x basasm

hackasm: addenv
	@. ../fun.sh && ../runsub.sh mkh
	@echo "cpm hackasm \$$1" > hackasm
	@. ../fun.sh && deltemp
	@. ../fun.sh && delenv
	@chmod +x hackasm

addenv:
	@. ../fun.sh && addenv

delenv:
	@. ../fun.sh && delenv

clean: delenv
	@. ../fun.sh && deltemp
	@rm -f hackasm basasm *.com
