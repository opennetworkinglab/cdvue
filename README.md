# cdvue

The Component Dependency Mapper (cdvue) is a tool that allows the user to determine how @Component classes relate to one another through @Reference tags and implementations of specific @Service classes in a specified path.

## Getting Started

To get started, clone this repository and set your bash_profile path variable to include ~/[your path]/cdvue/bin/cdvue

### How to Use

After setting your path, you can simply use the terminal command "cdvue" followed by a path to a directory of a Java project to analyze. An HTML file will be generated and opened to view the how the Component classes in the project relate to one another.

If you want a full log of everything that is going on while generating the HTML file, simply add "cdvueDebug=true" before the "cdvue" command.

## Built With

* QDox - A parser for Java files
* D3 - A JavaScript mapping library
* Maven

## Contributing

Get involved! Whether you are an individual or an organization, come on-board and contribute to [ONOS](http://onosproject.org/contribute/).

## Version

1.0

## Authors

* **Parth Pendurkar** - [GitHub](https://github.com/parp1)
* **Thomas Vachuska** - [GitHub](https://github.com/tomikazi)

Check out onos - [GitHub](https://github.com/opennetworkinglabs)

## License

Copyright 2015-present Open Networking Laboratory

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License [here] (http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Acknowledgments

* Thanks to Thomas Vachuska for all the help!
* Inspired by [jdvue](https://github.com/opennetworkinglab/jdvue).
