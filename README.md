<h3 align="center">
  BOOK EDITOR
</h3>

<p align="center">
  <a href="https://fabricmc.net/">
    <img src="https://img.shields.io/badge/Fabric-1.20.1-blue?style=for-the-badge&logo=fabric" alt="Fabric 1.20.1"/>
  </a>
</p>

## Description

Book Editor adds a WYSIWYG-like editor to a creative book item: format text, insert and position images, draw on a canvas, set page backgrounds, and manage multiple pages.  
You can sign a book to lock it from editing, automatically show the author badge, and have the item display the book’s title.

- Rich text: bold, italic, underline, color, size, alignment
- Images: insert by URL, absolute positioning, prefetch
- Canvas: brush tool with size/color, per-page background color
- Pages: add/delete, navigate, undo/redo
- Signing: lock edits, show author and title as the item’s name
- Icon swap: editable vs signed book icon via model predicate

---

<p align="center">
  <img src="https://github.com/user-attachments/assets/a1a888b8-7bd9-455c-a9fc-d6a88309d64c" width="700" alt="Editor Overview"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/39b09266-d3a0-43a6-8475-8982090fd7de" width="600" alt="Page View and Tools"/>
</p>

## Crafting

The Creative Book can be crafted using:
- 1 Feather
- 1 Paper
- 1 Gold Nugget

Arrange these items in any shape in the crafting grid to obtain the book.

<p align="center">
  <img src="https://github.com/user-attachments/assets/4a558f7e-e31e-40fd-b114-f69952e0e49a" width="600" alt="Book Crafting and Formatting"/>
</p>

## How to use

- Get the item: Craft it, find in creative inventory, or use command: `/give @p bookeditor:creative_book`
- Open the editor by using the item.
- Toolbars:
  - Formatting: bold/italic/underline, color, size, alignment
  - Canvas: brush on/off, brush size, background color, insert image, new/delete page, sign
- Keyboard:
  - <kbd>Ctrl</kbd>+<kbd>B</kbd> / <kbd>Ctrl</kbd>+<kbd>I</kbd> / <kbd>Ctrl</kbd>+<kbd>U</kbd> — toggles bold/italic/underline
  - <kbd>Ctrl</kbd>+<kbd>Z</kbd> / <kbd>Ctrl</kbd>+<kbd>Y</kbd> — undo/redo
  - <kbd>Ctrl</kbd>+<kbd>Enter</kbd> — sign the book
- After signing:
  - The book becomes read-only
  - Author badge is shown
  - Item name displays the book title

## Installation

Requirements:
- Minecraft: 1.20.1
- Java: 17
- Fabric Loader: 0.17.2 or newer
- Fabric API: 1.20.1 build (recommended ≥ 0.83.0+1.20.1)

Steps:
1. Install Fabric Loader for 1.20.1.
2. Put Fabric API for 1.20.1 in the `mods` folder.
3. Put `bookeditor-<version>.jar` in the `mods` folder.

## Features in detail

- **Text editing**
  - Rich formatting with live rendering
  - Undo/Redo stack
  - Multi-line text with alignment
- **Images**
  - Insert by URL, absolute positioning
  - Prefetches textures to avoid delays when flipping pages
- **Drawing canvas**
  - Brush on/off indicator, size control
  - Per-page background color
- **Pages and navigation**
  - Add/Delete pages
  - Jump to page, previous/next
- **Signing and display**
  - Signing locks editing, records author name/UUID
  - Item name shows the book title based on NBT (like vanilla signed books)
  - Item icon switches between editable/signed models via a custom predicate

## Localization

- Included: English (`en_us`), Russian (`ru_ru`)
- Easy to add more: create a new JSON in `assets/bookeditor/lang/<locale>.json` and translate keys

## Build from source

- JDK 17 required
- Commands:
  - Build: `./gradlew clean build`
  - Run client: `./gradlew runClient`
- Output:
  - `build/libs/bookeditor-<version>.jar` — use this in `mods`
  - `build/libs/bookeditor-<version>-sources.jar` — sources (for developers)

## License

Apache-2.0 © [Iliiasik](https://github.com/Iliiasik)
