#!/usr/bin/env python

import argparse
from subprocess import call


def setup(flags):
    if flags.gradle:
        return_code = install_gradle()

        if return_code != 0:
            print "Could not install Gradle"
            return return_code

    return_code = run_setup(flags)
    if return_code != 0:
        print "Could not install libraries"
        return return_code

    return_code = gradle_build(flags)
    if return_code != 0:
        print "Could not build the application"
        return return_code

    return 0


def install_gradle():
    return call(["./install_gradle.sh"])


def run_setup(flags):
    if flags.java:
        return call(["./setup.sh", flags.java])
    else:
        return call(["./setup.sh"])


def gradle_build(flags):
    if flags.gradle:
        return call(["gradle/bin/gradle", "build"])
    else:
        return call(["gradle", "build"])


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Personalized Automatic Speech Recognition setup")

    parser.add_argument("--gradle-install", dest="gradle", action="store_true",
                        help="Install gradle before setup")

    parser.add_argument("--java-path", type=str, dest="java", help="Java 8 SDK path")

    setup(parser.parse_args())
