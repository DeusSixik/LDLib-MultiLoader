# ChangeLogs

## v1.0.47
* Fixed a Mixin issue while loading massive mods

## v1.0.46
* Fixed DummyWorld memory leak
* Fixed KJSPlugin wrapper for LDLib FluidStack

## v1.0.45
* Fixed crash L2Hostility by Refactor VirtualChunk to fix. (thanks to @Taskeren)
* Fixed LabelWidget::detectAndSendChanges logic when using Component (thanks to @snylonue)

## v1.0.44
* Fixed crash L2Hostility.
* Fix UI rounding error and Crash related to SelectorWidget (thanks to @cr3eperall)

## v1.0.43
* Fixed crash L2Hostility, which tried to access level during virtual chunk <init>.
* Fixed crash with jGUI, they don't use mixin correctly
* Avoid using stream api for LinkedHashMap

## v1.0.42
* Fixed mod loading check during mixin

## v1.0.41.b
* Fixed RPCMethod crash

## v1.0.41.a
* Fixed DraggableScrollableWidget crash with client-side code

## v1.0.41
* Improve transform APIs

## v1.0.40.b
* Fixed world manager may be `null` while rendering the world scene

## v1.0.40.a
* Fixed fluid stack configurator amount limitation
* Refactored the CTM model implementation to use the loader's API
* Fixed unnecessary left-click requirement in ButtonWidget
* Fixed modular recipe widgets closing the current screen entirely when E or esc is pressed
* Fixed ModularWrapperWidget ignoring tooltips widgets that are inside scrollable widget groups

## v1.0.40
* Fixed world scene renderer issues

## v1.0.39.a
* Fixed EMI Crash

## v1.0.39
* Fixed + Improved EMI compatibility (thanks to @PrototypeTrousers)
* Fixed Number Configurator doesn't support Long
* Allow separated bg for player inventory

## v1.0.38.d
* Fixed editor resource rename doesn't work
