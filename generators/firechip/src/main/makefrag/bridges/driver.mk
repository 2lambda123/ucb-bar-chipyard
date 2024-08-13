# See LICENSE for license details.

##########################
# Driver Sources & Flags #
##########################

driver_dir = $(firesim_base_dir)/src/main/cc

firechip_lib_dir = $(chipyard_dir)/generators/firechip/src/main/cc
testchipip_csrc_dir = $(chipyard_dir)/generators/testchipip/src/main/resources/testchipip/csrc

DRIVER_H = $(shell find $(driver_dir) -name "*.h")

# fesvr and related srcs
DRIVER_CC += \
		$(testchipip_csrc_dir)/testchip_tsi.cc \
		$(testchipip_csrc_dir)/testchip_htif.cc \
		$(firechip_lib_dir)/fesvr/firesim_tsi.cc \
		$(RISCV)/lib/libfesvr.a
TARGET_CXX_FLAGS += \
		-isystem $(testchipip_csrc_dir) \
		-isystem $(RISCV)/include
TARGET_LD_FLAGS += \
		-L$(RISCV)/lib \
		-Wl,-rpath,$(RISCV)/lib

# top-level testing sources
DRIVER_CC += \
		$(wildcard $(addprefix $(firechip_lib_dir)/, \
			bridges/test/BridgeHarness.cc \
			bridges/test/$(DESIGN).cc \
		))
TARGET_CXX_FLAGS += \
		-I$(firechip_lib_dir)/bridge/test

# bridge sources
# exclude the following types of files for unit testing
EXCLUDE_LIST := cospike dmibridge groundtest simplenic tsibridge
DRIVER_CC += \
		$(filter-out \
			$(addprefix $(firechip_lib_dir)/bridges/,$(addsuffix .cc,$(EXCLUDE_LIST))), \
			$(wildcard \
				$(addprefix \
					$(firechip_lib_dir)/, \
					$(addsuffix .cc,bridges/* bridges/tracerv/*) \
				) \
			) \
		)
TARGET_CXX_FLAGS += \
		-I$(firechip_lib_dir) \
		-I$(firechip_lib_dir)/bridge \
		-I$(firechip_lib_dir)/bridge/tracerv
TARGET_LD_FLAGS += \
		-l:libdwarf.so -l:libelf.so

# other changes
TARGET_CXX_FLAGS += \
		-I$(driver_dir)/midasexamples \
		-I$(driver_dir) \
		-I$(driver_dir)/bridges \
		-g
