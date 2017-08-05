# CubScout
Cubscout is an application for scouting matches in FIRST Robotics Competition. Scouting is the process of tracking the performance of other robotics teams in their matches in order to make educated decisions. Cubscout focuses on alliance selection, wherin the higher-ranking teams, after qualifying rounds, choose teams for their alliance of three to compete in the playoffs.

# Motivation
Many teams, ours formerly included, use paper scouting forms and simple spreadsheets for scouting. This is an extremely cumbersome process and locks teams in to tracking what they expected to be important at the time they printed off the scouting sheets. Cubscout aims to allow teams to track detailed information that is important to them and change what information is relevent without losing the progress they made in earlier matches.

Many of the existing scouting solutions did very little actual data processing, instead serving purely as data collection to be later used in a spreadsheet. While this alleviates some of the logistical problems of the conventional paper+Excel method, spreadsheets are not a convienient data processing method, especially when a team wants to change how the data is processed. Cubscout's primary purpose is to do the hard work in processing scouting information while providing the flexibility possible in spreadsheet-based methods.

# Contributing
CubScout is currently in the pre-release stage. You can [create a new issue](https://github.com/robocubs4205/cubscout-server/issues) for proposed features, bugs, etc. as well as comment on [existing issues](https://github.com/robocubs4205/cubscout-server/issues). You can also contribute code by creating a fork of the repository with the fork button at the top of the page. You can make your changes to your fork then [create a pull request](https://github.com/robocubs4205/cubscout-server/pulls) to have your changes integrated into the project. Take a look at the [issues](https://github.com/robocubs4205/cubscout-server/issues) for information on what needs to be done, and please [create a new issue](https://github.com/robocubs4205/cubscout-server/issues) and discuss it with a core team member before starting on a feature that does not already have an issue.

# Building and Running
This project uses sbt for building. sbt must be installed to build CubScout. run the command `sbt` to create an sbt shell. In the sbt shell, run the command `play` to start Cubscout in development mode and run `test` to run tests.

# License
This project is licensed under the [Mit License](https://github.com/robocubs4205/cubscout-server/blob/develop/LICENSE).
