package com.diffeo.dossier.fc;

import com.fasterxml.jackson.annotation.JsonSubTypes;

public interface Feature {
    public boolean isReadOnly();
    public void setReadOnly(boolean ro);
}
