package manyminds.agents;

public interface
MutableAttributes
extends Attributes {
    public void set(String k, Object v);
    public void clearAll();
}