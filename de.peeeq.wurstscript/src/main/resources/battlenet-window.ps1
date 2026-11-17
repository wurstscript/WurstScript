# Keeps the Battle.net app's window out of sight while a sign-in renewal asks it to start Warcraft III.
#
# Asked to start a game, the app creates its window and shows it in front, and depending on its "when I launch a game"
# setting leaves it there. Started just before the renewal, this hooks window creation: each new top-level app window
# is moved off-screen as it is created, so it is shown out of sight, and then minimized without being activated. Its
# restore position is set back to where the app put it, so the user's own next Restore opens it normally. An app window
# that was already open is minimized too if it takes the foreground, and left alone otherwise. With -FocusGame it brings
# the map's Warcraft III client to the front once that has a window: a client that started after this script and not
# from the app, so never the user's own game or the renewal's short-lived one.
#
# The app shows its window again as the map's client signs in, so the watcher ends $GameSeconds after that client
# (one that started after this script and not from the app) appears, or at $Seconds if none does. Until that client
# appears, a new crash reporter (BlizzardError.exe) of the renewal's closed client is closed; any other is left alone.
#
# Prints "ready" once it has noted the windows already open, so the caller can ask the app only then; with -Log also
# one line per action. WurstSetup embeds a copy; keep it self-contained.
param([int]$Seconds = 20, [int]$GameSeconds = 60, [switch]$FocusGame, [switch]$Log)
$ErrorActionPreference = "Stop"
Add-Type @"
using System; using System.Collections.Generic; using System.Diagnostics; using System.Runtime.InteropServices;
public static class BattleNetWindow {
  delegate bool EnumProc(IntPtr h, IntPtr p);
  delegate void WinEventProc(IntPtr hook, uint ev, IntPtr hwnd, int idObject, int idChild, uint thread, uint time);
  [DllImport("user32.dll")] static extern bool EnumWindows(EnumProc f, IntPtr p);
  [DllImport("user32.dll")] static extern uint GetWindowThreadProcessId(IntPtr h, out uint pid);
  [DllImport("user32.dll")] static extern bool IsWindow(IntPtr h);
  [DllImport("user32.dll")] static extern bool IsWindowVisible(IntPtr h);
  [DllImport("user32.dll")] static extern bool IsIconic(IntPtr h);
  [DllImport("user32.dll")] static extern IntPtr GetWindow(IntPtr h, uint cmd);
  [DllImport("user32.dll")] static extern IntPtr GetAncestor(IntPtr h, uint flags);
  [DllImport("user32.dll")] static extern IntPtr GetForegroundWindow();
  [DllImport("user32.dll")] static extern bool SetForegroundWindow(IntPtr h);
  [DllImport("user32.dll")] static extern bool ShowWindow(IntPtr h, int cmd);
  [DllImport("user32.dll")] static extern bool ShowWindowAsync(IntPtr h, int cmd);
  [DllImport("user32.dll")] static extern bool GetWindowRect(IntPtr h, out RECT r);
  [DllImport("user32.dll")] static extern bool SetWindowPos(IntPtr h, IntPtr after, int x, int y, int cx, int cy, uint flags);
  [DllImport("user32.dll")] static extern bool GetWindowPlacement(IntPtr h, ref WINDOWPLACEMENT p);
  [DllImport("user32.dll")] static extern bool SetWindowPlacement(IntPtr h, ref WINDOWPLACEMENT p);
  [DllImport("user32.dll")] static extern IntPtr SetWinEventHook(uint min, uint max, IntPtr mod, WinEventProc proc, uint pid, uint tid, uint flags);
  [DllImport("user32.dll")] static extern bool UnhookWinEvent(IntPtr hook);
  [DllImport("user32.dll")] static extern uint MsgWaitForMultipleObjects(uint n, IntPtr[] h, bool all, uint ms, uint mask);
  [DllImport("user32.dll")] static extern bool PeekMessage(out MSG m, IntPtr h, uint min, uint max, uint remove);
  [DllImport("user32.dll")] static extern bool TranslateMessage(ref MSG m);
  [DllImport("user32.dll")] static extern IntPtr DispatchMessage(ref MSG m);
  [DllImport("kernel32.dll")] static extern IntPtr CreateToolhelp32Snapshot(uint flags, uint pid);
  [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern bool Process32FirstW(IntPtr snap, ref PROCESSENTRY32 e);
  [DllImport("kernel32.dll", CharSet = CharSet.Unicode)] static extern bool Process32NextW(IntPtr snap, ref PROCESSENTRY32 e);
  [DllImport("kernel32.dll")] static extern bool CloseHandle(IntPtr h);
  [StructLayout(LayoutKind.Sequential)] struct RECT { public int Left, Top, Right, Bottom; }
  [StructLayout(LayoutKind.Sequential)] struct POINT { public int X, Y; }
  [StructLayout(LayoutKind.Sequential)] struct MSG { public IntPtr hwnd; public uint message; public IntPtr wParam, lParam; public uint time; public POINT pt; }
  [StructLayout(LayoutKind.Sequential)] struct WINDOWPLACEMENT { public int length, flags, showCmd; public POINT min, max; public RECT normal; }
  [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)] struct PROCESSENTRY32 {
    public uint dwSize, cntUsage, th32ProcessID; public IntPtr th32DefaultHeapID; public uint th32ModuleID, cntThreads,
    th32ParentProcessID; public int pcPriClassBase; public uint dwFlags;
    [MarshalAs(UnmanagedType.ByValTStr, SizeConst = 260)] public string szExeFile; }

  const uint EVENT_CREATE = 0x8000, EVENT_SHOW = 0x8002, EVENT_LOCATION = 0x800B;
  const uint SWP_MOVE_ONLY = 0x0001 | 0x0004 | 0x0010; // NOSIZE | NOZORDER | NOACTIVATE
  const int OFF_SCREEN = -32000;
  static readonly Stopwatch watch = new Stopwatch();
  static readonly Dictionary<IntPtr, RECT> moved = new Dictionary<IntPtr, RECT>();
  static readonly HashSet<IntPtr> handled = new HashSet<IntPtr>();
  static HashSet<IntPtr> background = new HashSet<IntPtr>();
  static HashSet<uint> appPids = new HashSet<uint>();
  static readonly HashSet<uint> notOurGame = new HashSet<uint>();
  static readonly HashSet<uint> oldReporters = new HashSet<uint>();
  static readonly HashSet<uint> earlierGames = new HashSet<uint>();
  static readonly HashSet<uint> renewalClients = new HashSet<uint>();
  static bool log;
  static WinEventProc callback;

  static void Note(string s) { if (log) Console.WriteLine("{0} {1:N2}", s, watch.Elapsed.TotalSeconds); }

  static Dictionary<uint, PROCESSENTRY32> Processes() {
    var all = new Dictionary<uint, PROCESSENTRY32>();
    IntPtr snap = CreateToolhelp32Snapshot(2, 0);
    if (snap == new IntPtr(-1)) return all;
    try {
      var e = new PROCESSENTRY32(); e.dwSize = (uint)Marshal.SizeOf(typeof(PROCESSENTRY32));
      for (bool ok = Process32FirstW(snap, ref e); ok; ok = Process32NextW(snap, ref e)) all[e.th32ProcessID] = e;
    } finally { CloseHandle(snap); }
    return all;
  }

  static HashSet<uint> Pids(Dictionary<uint, PROCESSENTRY32> all, string exe) {
    var pids = new HashSet<uint>();
    foreach (var e in all.Values) if (string.Equals(e.szExeFile, exe, StringComparison.OrdinalIgnoreCase)) pids.Add(e.th32ProcessID);
    return pids;
  }

  // Visible, not minimized, unowned top-level windows of the given processes.
  static List<IntPtr> Shown(HashSet<uint> pids) {
    var found = new List<IntPtr>();
    EnumWindows((h, p) => {
      uint pid; GetWindowThreadProcessId(h, out pid);
      if (pids.Contains(pid) && IsWindowVisible(h) && !IsIconic(h) && GetWindow(h, 4) == IntPtr.Zero) found.Add(h);
      return true;
    }, IntPtr.Zero);
    return found;
  }

  static bool AppWindow(IntPtr h) {
    uint pid; GetWindowThreadProcessId(h, out pid);
    return appPids.Contains(pid) && GetAncestor(h, 2) == h && GetWindow(h, 4) == IntPtr.Zero;
  }

  // Minimizes without activating; the window holding the foreground is minimized normally, which hands it on.
  static void Minimize(IntPtr h, string why) {
    bool foreground = GetForegroundWindow() == h;
    ShowWindow(h, foreground ? 6 : 7);
    handled.Add(h);
    RECT original;
    if (moved.TryGetValue(h, out original) && original.Right > original.Left) {
      var p = new WINDOWPLACEMENT(); p.length = Marshal.SizeOf(p);
      if (GetWindowPlacement(h, ref p)) { p.normal = original; p.showCmd = 7; SetWindowPlacement(h, ref p); }
    }
    Note("minimized " + why);
  }

  static void OnEvent(IntPtr hook, uint ev, IntPtr h, int idObject, int idChild, uint thread, uint time) {
    if (idObject != 0 || idChild != 0 || h == IntPtr.Zero || !AppWindow(h)) return;
    RECT r; GetWindowRect(h, out r);
    bool onScreen = r.Left > OFF_SCREEN / 2;
    if (ev == EVENT_CREATE) {
      if (!moved.ContainsKey(h)) {
        moved[h] = r;
        SetWindowPos(h, IntPtr.Zero, OFF_SCREEN, OFF_SCREEN, 0, 0, SWP_MOVE_ONLY);
      }
      return;
    }
    if (!IsWindowVisible(h)) {
      // Not shown yet: the app may put it back where it last was; keep it off-screen and remember that as its place.
      if (ev == EVENT_LOCATION && moved.ContainsKey(h) && !handled.Contains(h) && onScreen) {
        moved[h] = r;
        SetWindowPos(h, IntPtr.Zero, OFF_SCREEN, OFF_SCREEN, 0, 0, SWP_MOVE_ONLY);
      }
      return;
    }
    if (IsIconic(h)) return;
    // Shown, or restored, in the open: unless it is a window the user had open in the background, move it out of
    // sight at once and minimize it. Its restore position stays the one the app first gave it.
    if (background.Contains(h) && GetForegroundWindow() != h) return;
    if (onScreen) {
      if (!moved.ContainsKey(h)) moved[h] = r;
      SetWindowPos(h, IntPtr.Zero, OFF_SCREEN, OFF_SCREEN, 0, 0, SWP_MOVE_ONLY);
    }
    Minimize(h, ev == EVENT_SHOW ? "shown window" : "restored window");
  }

  public static void Prepare(bool logActions) {
    log = logActions;
    var all = Processes();
    appPids = Pids(all, "Battle.net.exe");
    foreach (uint pid in Pids(all, "Warcraft III.exe")) { notOurGame.Add(pid); earlierGames.Add(pid); }
    foreach (uint pid in Pids(all, "BlizzardError.exe")) oldReporters.Add(pid);
    IntPtr foreground = GetForegroundWindow();
    background = new HashSet<IntPtr>(Shown(appPids));
    background.Remove(foreground);
  }

  public static void Run(int seconds, int gameSeconds, bool focusGame) {
    watch.Start();
    callback = OnEvent;
    IntPtr hook = SetWinEventHook(EVENT_CREATE, EVENT_LOCATION, IntPtr.Zero, callback, 0, 0, 0);
    bool focused = !focusGame;
    long lastScan = -1000;
    double gameSeenAt = -1;
    try {
      // The app shows its window again as the map's client signs in, so watch until that client has run a while.
      while (watch.Elapsed.TotalSeconds < seconds && (gameSeenAt < 0 || watch.Elapsed.TotalSeconds - gameSeenAt < gameSeconds)) {
        MsgWaitForMultipleObjects(0, null, false, 25, 0x04FF);
        MSG m;
        while (PeekMessage(out m, IntPtr.Zero, 0, 0, 1)) { TranslateMessage(ref m); DispatchMessage(ref m); }
        if (watch.ElapsedMilliseconds - lastScan < 50) continue;
        lastScan = watch.ElapsedMilliseconds;
        var all = Processes();
        appPids = Pids(all, "Battle.net.exe");
        IntPtr foreground = GetForegroundWindow();
        // An app window that was open already, or one the hook missed: minimize it if it is in the way.
        foreach (IntPtr h in Shown(appPids)) {
          if (background.Contains(h) && h != foreground) continue;
          Minimize(h, "shown window");
        }
        var ours = new HashSet<uint>();
        foreach (uint pid in Pids(all, "Warcraft III.exe")) {
          if (notOurGame.Contains(pid)) continue;
          PROCESSENTRY32 e; PROCESSENTRY32 parent;
          if (all.TryGetValue(pid, out e) && all.TryGetValue(e.th32ParentProcessID, out parent)
              && string.Equals(parent.szExeFile, "Battle.net.exe", StringComparison.OrdinalIgnoreCase)) {
            notOurGame.Add(pid);
            renewalClients.Add(pid);
            continue;
          }
          ours.Add(pid);
        }
        if (ours.Count > 0 && gameSeenAt < 0) gameSeenAt = watch.Elapsed.TotalSeconds;
        if (gameSeenAt < 0) {
          // A new crash reporter for the renewal's closed client: its parent is that client, or (seen too briefly to be
          // noted) a process already gone that was not one of the games running before. Any other reporter is left alone.
          foreach (uint pid in Pids(all, "BlizzardError.exe")) {
            if (oldReporters.Contains(pid)) continue;
            oldReporters.Add(pid);
            PROCESSENTRY32 reporter;
            if (!all.TryGetValue(pid, out reporter)) continue;
            uint parentPid = reporter.th32ParentProcessID;
            bool renewals = renewalClients.Contains(parentPid) || (!all.ContainsKey(parentPid) && !earlierGames.Contains(parentPid));
            if (!renewals) continue;
            try { Process.GetProcessById((int)pid).Kill(); Note("closed crash reporter"); } catch (Exception) { }
          }
        }
        if (!focused) {
          var game = Shown(ours);
          if (game.Count > 0) {
            if (!game.Contains(foreground)) SetForegroundWindow(game[0]);
            focused = true;
            Note("focused");
          }
        }
      }
    } finally {
      UnhookWinEvent(hook);
      // Windows the app created but never showed go back where it put them.
      foreach (var entry in moved) {
        if (!handled.Contains(entry.Key) && IsWindow(entry.Key)) {
          SetWindowPos(entry.Key, IntPtr.Zero, entry.Value.Left, entry.Value.Top, 0, 0, SWP_MOVE_ONLY);
        }
      }
    }
  }
}
"@
[BattleNetWindow]::Prepare([bool]$Log)
"ready"
[BattleNetWindow]::Run($Seconds, $GameSeconds, [bool]$FocusGame)
