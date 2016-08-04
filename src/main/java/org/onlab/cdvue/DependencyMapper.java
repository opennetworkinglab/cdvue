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

/**
 * Class that runs the Dependency Mapper Tool.
 *
 * @author Parth Pendurkar
 * @version 1.0
 */
public class DependencyMapper {

    private void processDirectory(String path) throws Exception {
        DependencyParser p = new DependencyParser(path);
        try {
            System.out.println("Executing.");
            p.execute();

            System.out.println("Execution complete. JSON's compiled.");
            System.out.println("Making Graph...");
            p.makeGraph();
        }
        catch (Exception e) {
            System.out.println("Execution failed.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DependencyMapper m = new DependencyMapper();
        try {
            String path = args[0];
            m.processDirectory(path);
        }
        catch (Exception e) {
            System.out.println("Could not process files...");
            e.printStackTrace();
        }
    }
}