/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onlab.cdvue;

import java.util.Objects;

/**
 * Class that runs the Dependency Mapper Tool.
 *
 * @author Parth Pendurkar
 * @version 1.0
 */
public class DependencyMapper {

    private static boolean debugOn = Objects.equals("true", System.getenv("cdvueDebug"));

    private void processDirectory(String path) throws Exception {
        DependencyParser p = new DependencyParser(path);
        try {
            println("Executing.");
            p.execute();

            println("Execution complete. JSON's compiled.");
            println("Making Graph...");
            p.makeGraph();
        }
        catch (Exception e) {
            println("Execution failed.");
            e.printStackTrace();
        }
    }

    static void println(String s) {
        if (debugOn) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
        DependencyMapper m = new DependencyMapper();
        try {
            String path = args[0];
            m.processDirectory(path);
        }
        catch (Exception e) {
            println("Could not process files...");
            e.printStackTrace();
        }
    }
}