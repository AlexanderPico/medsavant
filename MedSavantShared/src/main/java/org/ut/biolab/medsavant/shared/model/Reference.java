/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;


/**
 *
 * @author Andrew
 */
public class Reference implements Serializable {

    private final int id;
    private final String name;
    private final String url;

    public Reference(int id, String name){
        this.id = id;
        this.name = name;
        this.url = "";
    }

    public Reference(int id, String name, String url){
        this.id = id;
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString(){
        return name;
    }

    public int getID(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getUrl() {
        return url;
    }
}
