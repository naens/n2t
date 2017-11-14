all: addenv
	@source ../fun.sh && ../runsub.sh mk
	@echo "cpm basasm \$$1" > basasm
	@echo "cpm hackasm \$$1" > hackasm
	@chmod +x basasm hackasm

basasm: addenv
	@source ../fun.sh && ../runsub.sh mkb
	@echo "cpm basasm \$$1" > basasm
	@source ../fun.sh && deltemp
	@chmod +x basasm

hackasm: addenv
	@source ../fun.sh && ../runsub.sh mkh
	@echo "cpm hackasm \$$1" > hackasm
	@source ../fun.sh && deltemp
	@chmod +x hackasm

addenv:
	@source ../fun.sh && addenv

delenv:
	@source ../fun.sh && delenv

clean: delenv
	@source ../fun.sh && deltemp
	@rm -f basasm basasm.com hackasm hackasm.com tabtest.com tooltest.com
