# Tonbot Trivia Plugin [![Build Status](https://travis-ci.org/lijamez/tonbot-plugin-trivia.svg?branch=master)](https://travis-ci.org/lijamez/tonbot-plugin-trivia)

```
Plugin is in development.
```

Play trivia with your guildmates.

## Features

### Multiple question types
#### Short answer
Questions which require users to enter the answer. Great for "fill in the blank" questions too.

```
[5 Points] What was the former name of Thomas Bergersen's 2011 album, Illusions? 

First to answer correctly within 30 seconds wins.
```

#### Multiple Choice
Answers are randomized. 
Trivia Topics can include several different correct and incorrect choices. Tonbot can randomly pick from these choices to show to players. 

```
[5 Points] What was Two Steps From Hell's first public album?

1: Illusions
2: SkyWorld
3: Halloween
4: Invincible
5: Archangel

First to answer correctly within 30 seconds wins.
```

## Scoring


#### Music Identification
Plays a song, starting at any point. Players are asked to identify the song name, artist, composer, etc.

```
[5 points] What is this song's name?

First to answer correctly within 30 seconds wins.
```

```
[5 points] Who is the composer of this song?

First to answer correctly within 30 seconds wins.
```

```
[5 points] Who is this song's artist?

First to answer correctly within 30 seconds wins.
```

```
[5 points] In what year was this song released?

First to answer correctly within 30 seconds wins.
```

### Custom Trivia Topics
Easily create your own trivia questions.

## Usage

To show all of the installed trivia topics, use the ``trivia topics`` command. 

To play a topic:
```
t, trivia play <topic>
```

If the trivia topic contains music, join a voice channel first.

## Trivia Topic Specification

A trivia topic is a folder which contains the following files:
* ``trivia.json`` Includes the trivia topic's metadata, such as topic name, version, and description.
* ``questions.json`` Contains the questions.
* ``music`` A folder which contains music for the music identification questions. Tracks used for music should have correct ID3 tags.

### trivia.json

Sample:
```json
{
  "title" : "Two Steps From Hell",
  "version" : "1",
  "description" : "Two Steps From Hell-related questions. Includes trivia about Nick Phoenix and Thomas Bergersen."
}
```

All fields are required.

### questions.json

Sample:
```
{
  "questions" : [
    ...
  ]
}
```

The structure of each question may differ depending on its type. All questions must have:

* A ``type`` field to indicate what kind of question it is which also dictates the question's structure. ``type`` can be one of the following:
  * ``multiple_choice``
  * ``short_answer``
  * ``music_identification``
* A ``points`` field to indicate the number of points that a question is worth. The score may be scaled.

#### short_answer

Must contain the following fields:
* ``type`` : String. Must be ``short_answer``
* ``question`` : String. The question to ask.
* ``answers`` : Array of acceptable answers. Answers are not case senstitive. Punctuation are ignored.

Example: 
```json
{
  "type" : "short_answer",
  "points" : 5,
  "question" : " What was the former name of Thomas Bergersen's 2011 album, Illusions?",
  "answers" : ["Nemesis II", "Nemesis 2"]
}
```

#### multiple_choice

Must contain the following fields:
* ``type`` : String. Must be ``multiple_choice``
* ``question`` : String. The question to ask.
* ``choices`` : Array of Choices.

A Choice is structured as follows:
* ``value`` : String. The choice.
* ``isCorrect`` : Boolean. Whether if this is one of the correct choices.

Sample:
```json
{
  "type" : "multiple_choice",
  "points" : 5,
  "question": "What was Two Steps From Hell's first public album?",
  "choices" : [
    {
      "value" : "Invincible",
      "isCorrect" : true
    },
    {
      "value" : "Illusions",
      "isCorrect" : false
    },
    {
      "value" : "Archangel",
      "isCorrect" : false
    },
    {
      "value" : "Demon's Dance",
      "isCorrect" : false
    },
    {
      "value": "Halloween",
      "isCorrect" : false
    },
    {
      "value": "SkyWorld",
      "isCorrect" : false
    }
  ]
}
```

#### music_identification

Must contain the following fields:
* ``type`` : String. Must be ``music_identification``
* ``track_path`` : String. A path to the track. The path is relative to the ``music`` folder.
* ``ask_for`` : List of strings. Must be one of: ``title``, ``album_name``, ``artist``, ``composer``,  ``release_year``. Tonbot will ask for one of these.

Example: 
```json
{
  "type":"music_identification",
  "points" : 5,
  "song":"mySong.mp3",
  "ask_for" : ["release_year", "title", "artist"]
}
```
