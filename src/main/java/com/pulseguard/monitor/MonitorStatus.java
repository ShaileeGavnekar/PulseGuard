package com.pulseguard.monitor;

public enum MonitorStatus {
    UP,       // last check returned a 2xx within the timeout
    DOWN,     // last check failed, timed out, or returned a non-2xx
    UNKNOWN   // never checked yet
}
