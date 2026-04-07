package com.productmanagement.catalog.product.support;

import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public abstract class ControllerTestSupport {

    protected ResultMatcher successTrue() {
        return jsonPath("$.success").value(true);
    }

    protected ResultMatcher successFalse() {
        return jsonPath("$.success").value(false);
    }

    protected ResultMatcher errorCode(String code) {
        return jsonPath("$.error.code").value(code);
    }

    protected ResultMatcher errorMessageExists() {
        return jsonPath("$.error.message").exists();
    }

    protected ResultMatcher dataPath(String path, Object value) {
        return jsonPath("$.data." + path).value(value);
    }

    protected ResultMatcher dataArraySize(int size) {
        return jsonPath("$.data.length()").value(size);
    }

    protected ResultMatcher dataArrayPath(int index, String path, Object value) {
        return jsonPath("$.data[" + index + "]." + path).value(value);
    }

    protected ResultMatcher isEmptyData() {
        return jsonPath("$.data").isEmpty();
    }

}
