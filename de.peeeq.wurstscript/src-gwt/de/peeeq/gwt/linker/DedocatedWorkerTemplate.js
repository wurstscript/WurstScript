// Workaround for generated code accessing window and document
// when it is not really window and document which are needed
self.$wnd = self;
self.$doc = self;

console.log("Starting wrapper");

function __MODULE_FUNC__() {
    console.log("Starting module func");
    var strongName;
    try {
        // __PERMUTATIONS_BEGIN__
        // Permutation logic
        // __PERMUTATIONS_END__
    } catch (e) {
        console.log("error", e);
        console.log("ERRROR strongName ", strongName);
        // intentionally silent on property failure
        return;
    }
    console.log("Selected strongName ", strongName);
    importScripts(strongName + ".cache.js");
    gwtOnLoad(undefined, '__MODULE_NAME__', '');
}
__MODULE_FUNC__();