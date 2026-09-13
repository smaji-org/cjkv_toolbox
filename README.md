# Smaji CJKV Toolbox

[![CI](https://github.com/smaji-org/cjkv_toolbox/actions/workflows/scala.yml/badge.svg)](https://github.com/smaji-org/cjkv_toolbox/actions/workflows/scala.yml)

Smaji CJKV Toolbox is a cross-platform module manager designed for CJKV (Chinese, Japanese, Korean, Vietnamese) related tools. It provides a unified platform to easily discover, install, update, and manage modules such as input methods, fonts, and other CJKV-related components.

## Features

- **Cross-Platform Support**: Works on various operating systems (Linux, FreeBSD, NetBSD, Windows, etc.).
- **Module Management**:
    - **Discovery**: Automatically fetches available modules from a central repository.
    - **Installation**: Easy one-click installation of modules.
    - Un**installation**: Easy one-click uninstallation of modules.
    - **Updates**: Keeps your modules up-to-date with the latest versions.
    - **Setup**: Handles platform-specific configurations.
- **Graphical User Interface**: A user-friendly Swing-based GUI for seamless interaction.
- **Native Integration**: Includes platform-specific helpers for seamless system integration (e.g., autostart on Linux or Windows, font registration, etc.).

## Project Structure

- `toolbox/`: The core application source code. This is the GUI tool that users interact with to manage modules.
- `installer/`: Source code for the toolbox installer, used to deploy the toolbox and its components to a target system.
- `native_aux/`: Platform-specific helper binaries for tasks like autostart configuration and Windows-specific font management.

## Technologies Used

- **Scala**: Main programming language for the toolbox and installer.
- **Mill**: Build tool for the Scala project.
- **Go**: Used for native platform helpers in `native_aux`.
- **Swing**: The GUI framework for the toolbox application.
- **Apache Commons Compress**: Used for handling compressed module archives.

## Development

### Prerequisites

- [Mill](https://mill-build.com/)
- JDK 11 or higher
- Go (for `native_aux` development)

### Building the Project

To build the project using Mill, run:

```bash
./mill toolbox.assembly
```

### Running the Toolbox

After building, you can run the toolbox from the generated artifacts. Or run it by

```bash
./mill toolbox.run
```

## License

This project is licensed under the [GPLv2](LICENSE) license.
