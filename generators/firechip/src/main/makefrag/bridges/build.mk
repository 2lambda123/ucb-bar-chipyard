# See LICENSE for license details.

.PHONY: force_rule_to_run
force_rule_to_run:
	echo "Forcing downstream rule to run"

# $(1) - source file to check if equal
# $(2) - dest file to check if equal. if unequal, copy source to dest.
define COPY_IF_NOT_EQUAL
  @md5sum1=$$(md5sum $(1) | cut -d' ' -f1); \
  md5sum2=$$(md5sum $(2) | cut -d' ' -f1); \
  if [ "$$md5sum1" != "$$md5sum2" ]; then \
    echo "$(1) and $(2) do not match ($$md5sum1 vs. $$md5sum2). Copying $(1) to $(2)."; \
    cp -f $(1) $(2); \
  else \
    echo "$(1) and $(2) match ($$md5sum1 vs. $$md5sum2). Doing nothing."; \
  fi
endef

CHIPYARD_STAGING_DIR := $(chipyard_dir)/sims/firesim-staging

.PHONY: copy_firesim_files
copy_firesim_files:
	rm -rf $(firesim_base_dir)/midas/src/main/scala/targetcopied
	mkdir -p $(firesim_base_dir)/midas/src/main/scala/targetcopied
	cp -r $(chipyard_dir)/generators/firechip-isolated/src/main/scala/* $(firesim_base_dir)/midas/src/main/scala/targetcopied
	cp -r $(chipyard_dir)/generators/firechip-firesim-only/src/main/scala/* $(firesim_base_dir)/midas/src/main/scala/targetcopied

# this rule always is run, but may not update the timestamp of the targets (depending on what the Chipyard make does).
# if that is the case (Chipyard make doesn't update it's outputs), then downstream rules *should* be skipped.
# all other chipyard collateral is located in chipyard's generated sources area.
$(FIRRTL_FILE) $(ANNO_FILE) &: SHELL := /usr/bin/env bash # needed for running source in recipe
$(FIRRTL_FILE) $(ANNO_FILE) &: force_rule_to_run copy_firesim_files
	@mkdir -p $(@D)
	source $(chipyard_dir)/env.sh && \
		make -C $(CHIPYARD_STAGING_DIR) \
			SBT_PROJECT=$(TARGET_SBT_PROJECT) \
			MODEL=$(DESIGN) \
			MODEL_PACKAGE=$(DESIGN_PACKAGE) \
			VLOG_MODEL=$(DESIGN) \
			CONFIG=$(TARGET_CONFIG) \
			CONFIG_PACKAGE=$(TARGET_CONFIG_PACKAGE) \
			GENERATOR_PACKAGE=chipyard \
			TB=unused \
			TOP=unused
	# $(long_name) must be same as Chipyard
	$(call COPY_IF_NOT_EQUAL,$(CHIPYARD_STAGING_DIR)/generated-src/$(long_name)/$(long_name).fir,$(FIRRTL_FILE))
	$(call COPY_IF_NOT_EQUAL,$(CHIPYARD_STAGING_DIR)/generated-src/$(long_name)/$(long_name).anno.json,$(ANNO_FILE))