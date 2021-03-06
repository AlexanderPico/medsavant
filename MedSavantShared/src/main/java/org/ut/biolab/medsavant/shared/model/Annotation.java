/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.net.URL;

import org.ut.biolab.medsavant.shared.format.AnnotationFormat.AnnotationType;


/**
 * @author mfiume
 */
public class Annotation implements Serializable {

    private final int id;
    private final String program;
    private final String version;
    private final int referenceID;
    private final String referenceName;
    private final String dataPath;
    private final AnnotationType type;
    private boolean isEndInclusive = false;

    public Annotation(int id, String program, String version, int refID, String refName, String dataPath, AnnotationType type, boolean isEndInclusive) {
        this.id = id;
        this.program = program;
        this.version = version;
        this.referenceID = refID;
        this.referenceName = refName;
        this.dataPath = dataPath;
        this.type = type;
        this.isEndInclusive = isEndInclusive;
    }

    public int getID() {
        return id;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getProgram() {
        return program;
    }

    public int getReferenceID() {
        return referenceID;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getVersion() {
        return version;
    }

    public boolean isInterval() {
        return type == AnnotationType.INTERVAL;
    }

    public AnnotationType getAnnotationType(){
        return type;
    }

    @Override
    public String toString() {
        return getProgram() + " (v" + getVersion() + ", " + getReferenceName() + ")";
    }

    public boolean isEndInclusive() {
        return isEndInclusive;
    }

}
