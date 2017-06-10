package com.actualize.mortgage.transformx;

public class UCDError {

    private Integer lineNumber;
    private String error;
    private boolean element;
    private boolean attribute;
    /**
     * @return the lineNumber
     */
    public Integer getLineNumber() {
        return lineNumber;
    }
    /**
     * @param lineNumber the lineNumber to set
     */
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    /**
     * @return the error
     */
    public String getError() {
        return error;
    }
    /**
     * @param error the error to set
     */
    public void setError(String error) {
        this.error = error;
    }
    /**
     * @return the element
     */
    public boolean isElement() {
        return element;
    }
    /**
     * @param element the element to set
     */
    public void setElement(boolean element) {
        this.element = element;
    }
    /**
     * @return the attribute
     */
    public boolean isAttribute() {
        return attribute;
    }
    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(boolean attribute) {
        this.attribute = attribute;
    }
}
