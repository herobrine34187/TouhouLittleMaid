package com.github.tartaricacid.touhoulittlemaid.ai.service;

import org.apache.commons.lang3.StringUtils;

public interface Model {
    Model EMPTY = new Model() {
        @Override
        public String name() {
            return StringUtils.EMPTY;
        }

        @Override
        public String value() {
            return StringUtils.EMPTY;
        }
    };

    String name();

    String value();
}
