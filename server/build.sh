#!/bin/sh

gcc -Wall src/main.c lib/cubiomes/libcubiomes.a -Ilib -fwrapv -lm -o build/prcserver