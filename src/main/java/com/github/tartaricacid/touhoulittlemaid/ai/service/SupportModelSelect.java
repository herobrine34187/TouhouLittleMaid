package com.github.tartaricacid.touhoulittlemaid.ai.service;

import java.util.List;

public interface SupportModelSelect<T extends Model> {
    List<T> models();

    default void addModel(T model) {
        if (models().stream().noneMatch(m -> m.value().equals(model.value()))) {
            models().add(model);
        }
    }

    default void removeModel(Model model) {
        models().removeIf(m -> m.value().equals(model.value()));
    }

    default T getDefaultModel() {
        return models().get(0);
    }

    default Model getModel(String value) {
        return models().stream()
                .filter(model -> model.value().equals(value))
                .findFirst()
                .orElse(getDefaultModel());
    }
}
