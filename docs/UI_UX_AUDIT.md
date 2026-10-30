# UI/UX AUDIT & VISUAL DESIGN REPORT: SAHAAYA COMPANION

**Application**: Sahaaya Frontline Healthcare Workflow Companion  
**Design System**: Material 3 Design Tokens, Custom High-Contrast Healthcare Palette  
**Auditor**: Lead Android UX & Accessibility Systems Engineer  
**Audit Date**: October 30, 2026  

---

## 1. Executive Summary

A comprehensive visual, layout, and ergonomic audit was performed on the previous prototype screens to address field usability issues on entry-level Android devices ($< 6.0$-inch displays, low PPI, variable density). All critical visual overflow, button compression, information density, and accessibility issues have been systematically resolved.

---

## 2. Issues Audited and Resolved

| # | Visual / Usability Issue | Previous Prototype State | Corrective Implementation in Sahaaya v1.2 | Verification Status |
|:---:|:---|:---|:---|:---:|
| 1 | **Visit-Card Horizontal Overflow** | Fixed width cards or rigid horizontal rows pushed buttons off-screen on narrow devices ($< 360$ dp width). | Cards converted to flexible column-weighted layouts with responsive padding ($12$ dp on compact screens) and adaptive button wrapping. | **RESOLVED & VERIFIED** |
| 2 | **Excessive Text Wrapping** | Long Hindi/English medical descriptions wrapped awkwardly into $2-3$ character snippets. | Added `maxLines = 1` with text truncation on headings; clinical descriptions given dedicated full-width background boxes with relaxed line heights ($16$ sp). | **RESOLVED & VERIFIED** |
| 3 | **Buttons Compressing Columns** | "Mark Visited" and "Refer Now" buttons squeezed the text container into unreadable narrow columns. | Split into dedicated full-width action rows beneath the patient description, giving both buttons equal 50% flex width (`Modifier.weight(1f)`). | **RESOLVED & VERIFIED** |
| 4 | **Inconsistent Card Heights** | Metric cards had differing vertical heights depending on language string lengths. | Set standardized fixed heights (`86.dp`) on all triage stat cards with vertical center alignment. | **RESOLVED & VERIFIED** |
| 5 | **Date and Heading Overflow** | Scheduled dates and household IDs overlapped in multilingual views. | Structured header into a two-column `SpaceBetween` row: Category badge on left, Household ID badge + Priority pill on right. | **RESOLVED & VERIFIED** |
| 6 | **Touch Target Accessibility** | Action buttons had touch target areas under $36 \times 36$ dp, causing mis-taps in moving field conditions. | All interactive touch targets (buttons, icon toggles, radio buttons) standardized to minimum $48 \times 48$ dp touch target boundaries. | **RESOLVED & VERIFIED** |
| 7 | **Visual Hierarchy & Contrast** | Monolithic text cards made emergency cases hard to distinguish in direct sunlight. | High-visibility color tokens: Red (`#DC2626`) for Emergency DANGER, Amber (`#D97706`) for OVERDUE, Emerald (`#059669`) for ROUTINE, with distinct squircle icon containers. | **RESOLVED & VERIFIED** |
| 8 | **Dark / Bright Mode Readability** | Dark mode lacked surface separation, making cards blend into the background. | Added `SurfaceElevatedDark` (`#1A2C42`) over `SurfaceDark` (`#0A192F`) with 1.5 dp borders on critical alerts. | **RESOLVED & VERIFIED** |

---

## 3. Screen-by-Screen UX Enhancements

### 3.1 Landing Screen (`LandingScreen.kt`)
- **First-Run Simplicity**: Clear identity squircle with bright mode and dark mode dynamic logos.
- **Offline Assurance**: Prominent green banner stating "100% OFFLINE READY: All 107 leaf protocols & 2,234 beneficiaries stored on device."
- **1-Tap Language Bar**: Instant selection between English, हिन्दी, and తెలుగు.
- **Worker Credential**: Displays authorized ASHA profile and assigned sector.

### 3.2 Main Dashboard (`DashboardScreen.kt`)
- **Field Metric Cards**: 3 prominent cards showing Emergency Alerts, Overdue Visits, and Routine Visits.
- **Instant Search**: Unified query bar searching across patient names, household IDs, and member IDs.
- **Quick Action Bar**: 1-tap "Ask Protocol" and "Visit Register" launchers.
- **Offline Event Monitor**: Real-time counter of pending audit queue records.

### 3.3 Patient Profile & Attachment Screen (`PatientProfileScreen.kt`)
- **Beneficiary Summary**: Clean patient banner showing name, sex, age, household ID, and pregnancy/ANC badge.
- **Timeline of Follow-ups**: Chronological list of scheduled visits with status badges (COMPLETED vs PENDING).
- **Interactive Outcome Recording**: Dialog with 6 standardized outcome categories (`Improved`, `Unchanged`, `Worsened`, `Referred`, `Not Available`).
- **Media Attachments**: Dedicated section to view, capture, and delete symptom observation photos and MCP card snapshots.
- **Referral Safeguard**: If outcome is marked `WORSENED` or `REFERRED`, an immediate red banner appears advising immediate 108 hospital referral.
