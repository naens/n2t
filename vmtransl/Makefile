all: vmtransl

vmtransl: addenv
	@. ../fun.sh && ../runsub.sh mk
	@echo "cpm vmtransl \$$1" > vmtransl
	@. ../fun.sh && deltemp
	@. ../fun.sh && delenv
	@chmod +x vmtransl

addenv:
	@. ../fun.sh && addenv

delenv:
	@. ../fun.sh && delenv

clean: delenv
	@. ../fun.sh && deltemp
	@rm -f hackasm basasm *.com vmtransl
