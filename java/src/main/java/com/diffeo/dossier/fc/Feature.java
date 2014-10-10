/**
 * dossier.fc Feature Collections
 *
 * This software is released under an MIT/X11 open source license.
 * Copyright 2014 Diffeo, Inc.
 *
 */

package com.diffeo.dossier.fc;

public interface Feature {
    public boolean isReadOnly();
    public void setReadOnly(boolean ro);
}
