AUDIO FILES FOR JMONKEY VIBE GAME
==================================

This folder should contain the audio files for the game.
The game will gracefully handle missing files (logging warnings but not crashing).

REQUIRED DIRECTORY STRUCTURE:
-----------------------------

Sounds/
  Music/
    exploration.ogg  - Ambient/relaxing background music for exploration mode
    combat.ogg       - Intense/action background music for dungeon combat

  Effects/
    player_shoot.ogg - Short sound when player fires a projectile (laser/magic)
    enemy_hit.ogg    - Sound when an enemy takes damage (impact/thud)
    enemy_death.ogg  - Sound when an enemy dies (explosion/death cry)
    player_hit.ogg   - Sound when player takes damage (grunt/pain)
    wave_start.ogg   - Announcement sound when a new wave begins (horn/alarm)

AUDIO FORMAT RECOMMENDATIONS:
-----------------------------

- Format: OGG Vorbis (.ogg) is recommended for best compatibility
- Alternative: WAV files also work but are larger
- Music: Can be longer tracks (1-3 minutes, looping seamlessly)
- Effects: Should be short (0.1-2 seconds typically)
- Sample Rate: 44100 Hz recommended
- Channels: Mono for effects, Stereo for music

FREE AUDIO RESOURCES:
---------------------

You can find free game audio at:
- OpenGameArt.org (https://opengameart.org)
- Freesound.org (https://freesound.org)
- Kenney.nl (https://kenney.nl/assets?q=audio)
- itch.io (search for "free music" or "free sfx")

VOLUME NOTES:
-------------

- Music volume is set to 50% by default
- Effects volume is set to 80% by default
- Both can be adjusted via AudioManager methods

If you don't have audio files, the game will still run normally,
just without sound. Warning messages will appear in the console
indicating which audio files are missing.
