The pipe update! Huge thanks to aderoian/Armen who did the majority of the work here. With this change and stability of the new energy API in the last update,
both the fabric and neoforge version are now available as beta versions instead of alpha.
Full changelog:
- Improved pipes (@aderoian):
  - Pipes can now be set to extract in specific directions only, allowing insertion and extraction in 1 block space
  - Pipe connections can be disabled/enabled with the new pipe wrench item
  - Add pipe wrench item
- Add redstone status panel to machine UIs
- Add different visuals for redstone addons, based on their redstone power state
- Add energite blocks glow
- Add specific sounds to more resource blocks
- Add polish translations for mod and wiki (@plhappylemonpl & @Przemol)
- Change collision box of ceiling lights, allowing players to walk under them in a 2-block space
- Change resource block translations (instead of 'X block' it's now 'Block of X')
- Fix redstone addon not working on enderic lasers
- Fix texture size of the tachyon collector (@Plompi)

Warning: Due to the pipe rework, their blockstate have been changed. This is a **breaking** change. All existing item and fluid pipes
will be set to connect at every single connection, and energy pipes need to be fully replaced/updated. Sadly things like this are hard to avoid during alpha/beta stages.