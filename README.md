# excentric-nfc-sonos

A multi-module project that allows you to control Sonos speakers using NFC tags. The project consists of several components:

1. A CLI application for managing album metadata and generating labels
2. An NFC module for reading and writing NFC tags
3. A PDF generator for creating printable labels for NFC tags
4. Integration with music services (Apple Music, MusicBrainz, Cover Art Archive)

## Project Overview

This project enables you to:
- Search for albums on MusicBrainz and save their metadata
- Download album cover art from Cover Art Archive
- Fetch Apple Music album IDs for saved albums
- Generate PDF labels for albums with cover art
- Write NFC tags with Apple Music album IDs
- Use NFC tags to control Sonos speakers (play specific albums)

## Setup

### Prerequisites
- Java 11 or higher
- Node.js and npm (for NFC functionality)
- NFC reader/writer hardware
- Sonos HTTP API running on a host named "bb" (or configure in settings)
- Avery 80x50 rectangle labels (for printing)

### Building the Project

This project uses [Gradle](https://gradle.org/).

* Run `./gradlew assemble` to build the application
* Run `./gradlew build` to build and run tests
* Run `./gradlew run` to build and run the CLI application

## Usage

### CLI Application

Run the CLI application:
```
./cli-app.sh
```

The CLI application provides the following commands:

#### Album Management
- `list-slots` (alias `ls`) - List all saved album metadata slots
- `move-slot` (alias `mv`) - Move a slot from one position to another
- `remove-slots` (alias `rm`) - Delete slots from the metadata directory

#### MusicBrainz Integration
- `mb-search` (alias `mbs`) - Search MusicBrainz for album and artist
- `mb-search-artist` (alias `mbsa`) - Search MusicBrainz by artist name

#### Cover Art Management
- `ca-download` (alias `cad`) - Download cover art from Cover Art Archive
- `ca-select` (alias `cas`) - List cover art files in a slot and select one
- `ca-open-folder` (alias `cao`) - Open folder with potential cover art

#### Apple Music Integration
- `am-fetch` - Fetch Apple Music album IDs for slots
- `am-set` - Manually set the Apple Music album ID for a slot

#### PDF Generation
- `pdf` - Create and open a PDF with labels for slots 1-10

### NFC Functionality

#### Reading NFC Tags
```
./read-tags.sh
```

This will start the NFC reader and listen for NFC tags. When a tag is detected, it will read the command from the tag and send it to the configured Sonos speaker.

#### Writing NFC Tags
```
./write-tags.sh
```

This will read album metadata from the `~/.malm/metadata` directory and write Apple Music album IDs to NFC tags when presented to the reader.

## Configuration

The NFC module can be configured by editing the `malm-nfc/settings.json` file:
```json
{
  "sonosRoom": "Your Sonos Room Name"
}
```

## Project Structure

- `malm-cli-app` - Spring Boot CLI application for managing metadata
- `malm-metadata` - Shared metadata models and utilities
- `malm-nfc` - TypeScript application for reading and writing NFC tags
- `malm-pdf` - PDF generation for album labels

## Building from Source

This project uses Gradle for building:

```
./gradlew build
```

The project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).
