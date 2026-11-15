# Derelict: Unofficial NeoForge Port

Unofficial port of **[Derelict](https://github.com/mim1q/Derelict)** by **Mim1q** (Fabric 1.21.1, version 2.1.0) to **NeoForge 1.21.1**.

## Status: early alpha

The port builds and passes client and dedicated-server smoke tests, including a joint run with the Mine Cells NeoForge port. Gameplay has not been exhaustively compared with the Fabric original: please report anything that behaves differently (see below).

## Requirements

- Minecraft 1.21.1, NeoForge 21.1.200 or newer
- [Kotlin for Forge](https://modrinth.com/mod/kotlin-for-forge) 5.x
- [oωo-lib](https://modrinth.com/mod/owo-lib) (NeoForge build) 0.12.15 or newer
- [TerraBlender](https://modrinth.com/mod/terrablender) (NeoForge build) 4.1.0.8 or newer

REI and EMI integrations are included and load only when the corresponding recipe viewer is installed.

## Getting textures and models

All Derelict textures and models are **All Rights Reserved** by Mim1q. They are **not included** in this port.

1. Download the original **Derelict 2.1.0** for **Fabric 1.21.1** yourself: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/derelict/files) or [Modrinth](https://modrinth.com/mod/derelict/version/2.1.0).
2. Put the downloaded `.jar` into your `resourcepacks` folder. **Not** into `mods`.

On startup the port finds the jar and loads **only its client assets** (textures, models, sounds, translations) as a built-in resource pack, on top of the port's own data pack. Nothing is downloaded by the mod itself. Without the jar the game works, but items and blocks use the missing-texture look, and a one-time notice on the title screen explains what to do.

gimm1q assets (MIT) are bundled with the port, so its highlight and particle textures work out of the box.

## Building

Requires JDK 21.

```bash
./gradlew build
```

The mod jar is in `build/libs/`.

## Reporting bugs

This is an **unofficial** port. Please **do not** report its bugs to Mim1q or on the original issue tracker. Use this repository instead: https://github.com/sanufsoiii/derelict-neoforge/issues

## License and credits

- Derelict was created by **Mim1q**: https://github.com/mim1q/Derelict. All gameplay design, art, sounds and names belong to them.
- This port is not affiliated with or endorsed by the original author.
- Code: **MIT**, same as the original code (see `LICENSE`). The bundled gimm1q library is also MIT (see `LICENSE-gimm1q`).
- Folder `_excluded/` keeps original Fabric-only metadata (`fabric.mod.json`, Yarn access widener) for reference; it is not compiled or packaged.
