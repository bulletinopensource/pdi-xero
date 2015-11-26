#
#    Copyright 2015 Bulletin.Net (NZ) Limited : www.bulletin.net
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# The project is built with Apache Maven, but the authors do not wish to use the Maven release plugin because
# it is difficult to configure the SCM outside of the "pom" file.  This script is instead used for release
# management in a local git repository.

import sys
import os
import re
import xml.etree.ElementTree as etree
import subprocess


# This will obtain the next version of the project with the "-SNAPSHOT" at the end.  It will return a regular
# expression match where the three groups found are the major, minor and micro version parts.

def obtaininitialversionmatch():
    ns = '{http://maven.apache.org/POM/4.0.0}'

    if not os.path.isfile("pom.xml"):
        print "the 'pom.xml' file should be accessible in the present working directory"
        sys.exit(1)

    rootpomtree = etree.parse("pom.xml")

    if not rootpomtree:
        print "the 'pom.xml' in the present working directory should be able to be parsed"
        sys.exit(1)

    initialversionel = rootpomtree.getroot().find('{0}version'.format(ns))

    if None is initialversionel:
        print "unable to find the top level project version."
        sys.exit(1)

    match = re.match("^([1-9][0-9]*)\.([0-9]+)\.([1-9][0-9]*)-SNAPSHOT$", initialversionel.text)

    if not match:
        print "the pom version is malformed; " + initialversionel.text
        sys.exit(1)

    return match


# this function will set the version of the project using the maven versions plugin.

def setversion(version):
    print "will set the version to; " + version

    if 0 == subprocess.call(["mvn", "-q", "versions:set", "-DnewVersion=" + version, "-DgenerateBackupPoms=false"]):
        print("set the version to; " + version)
    else:
        print("unable to set the version to; " + version)
        sys.exit(1)


def gitadd():
    if 0 == subprocess.call(["git", "add", "pom.xml"]):
        print("git add pom.xml")
    else:
        print("unable to git add pom.xml")
        sys.exit(1)


def gitcommit(version):
    if 0 == subprocess.call(["git", "commit", "-m", version]):
        print("commit")
    else:
        print("unable to commit")
        sys.exit(1)


def gittag(version):
    if 0 == subprocess.call(["git", "tag", "-a", version, "-m", version]):
        print("tag the version at; " + version)
    else:
        print("unable to tag the version at; " + version)
        sys.exit(1)


# now we want to set the version

initialversionmatch = obtaininitialversionmatch() # regex
currentversion = initialversionmatch.group(1) + '.' + initialversionmatch.group(2) + '.' + initialversionmatch.group(3)
nextversionsnapshot = initialversionmatch.group(1) + '.' + initialversionmatch.group(2) + '.' + str(int(initialversionmatch.group(3))+1) + '-SNAPSHOT'
print("initial / current / next; " + initialversionmatch.group(0) + ", " + currentversion + ", " + nextversionsnapshot)

setversion(currentversion)
gitadd()
gitcommit(currentversion)
gittag(currentversion)
setversion(nextversionsnapshot)
gitadd()
gitcommit(nextversionsnapshot)

print "---------------"
print "to complete the release; git push && git push --tags"

