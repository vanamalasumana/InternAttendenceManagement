# SyncIn Admin Dashboard — Component Library
## Modern, Production-Ready Components

---

## 📦 Component Overview

This document showcases all improved components used in the redesigned Admin Sudo interface.

---

## 🔘 Buttons

### 1. Primary Button (Call-to-Action)
**Usage:** Main actions like "Create POC", "Sign In"

```html
<button class="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 
  hover:to-blue-800 active:from-blue-800 active:to-blue-900 text-white font-semibold 
  py-2.5 rounded-lg transition-all duration-200 shadow-md hover:shadow-lg 
  hover:-translate-y-0.5 active:shadow-sm active:translate-y-0">
  🚀 Create POC Account
</button>
```

**States:**
- **Default:** Gradient blue with light shadow
- **Hover:** Darker gradient, increased shadow, lifts 2px
- **Active:** Darkest gradient, minimal shadow, presses down
- **Disabled:** `opacity-50 cursor-not-allowed`

---

### 2. Secondary Button (Actions)
**Usage:** Reload, secondary actions

```html
<button class="inline-flex items-center gap-2 px-3 py-2 bg-blue-50 
  text-blue-700 hover:bg-blue-100 rounded-lg font-medium transition-all text-sm hover:shadow-md">
  🔄 Reload
</button>
```

**States:**
- **Default:** Light blue background + dark blue text
- **Hover:** Darker blue background + subtle shadow
- **Active:** Slight color shift

---

### 3. Destructive Button (Logout)
**Usage:** Logout, delete actions

```html
<button class="inline-flex items-center gap-2 px-4 py-2 bg-red-50 text-red-700 
  hover:bg-red-100 rounded-lg font-medium transition-colors text-sm">
  🚪 Logout
</button>
```

**States:**
- **Default:** Light red background + dark red text
- **Hover:** Darker red background
- **Focus:** Outline or ring for accessibility

---

## 📝 Form Inputs

### 1. Text Input (Default)
```html
<div class="group">
  <label class="block text-sm font-semibold text-gray-900 mb-1.5">
    Employee ID <span class="text-red-500">*</span>
  </label>
  <div class="relative">
    <input type="text" placeholder="e.g., POC001"
      class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
      text-sm text-gray-900 placeholder-gray-400 transition-all focus:outline-none 
      focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:bg-white" />
    <span class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 text-lg">👤</span>
  </div>
</div>
```

**Features:**
- 2px border (not 1px - more prominent)
- Rounded corners: 8px
- Padding: 10px 16px (comfortable for touch)
- Icon on right side
- Focus: blue border + soft blue ring + white background

### 2. Email Input
```html
<div class="group">
  <label class="block text-sm font-semibold text-gray-900 mb-1.5">
    Email Address <span class="text-red-500">*</span>
  </label>
  <input type="email" placeholder="e.g., priya@company.com"
    class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
    text-sm text-gray-900 placeholder-gray-400 transition-all focus:outline-none 
    focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:bg-white" />
</div>
```

### 3. Password Input
```html
<div class="group">
  <label class="block text-sm font-semibold text-gray-900 mb-1.5">
    Password <span class="text-red-500">*</span>
  </label>
  <div class="relative">
    <input type="password" placeholder="Minimum 8 characters"
      class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
      text-sm text-gray-900 placeholder-gray-400 transition-all focus:outline-none 
      focus:border-blue-500 focus:ring-2 focus:ring-blue-200 focus:bg-white" />
    <span class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 text-lg">🔑</span>
  </div>
</div>
```

---

## 📋 Cards

### 1. Main Card Container
```html
<div class="bg-white rounded-2xl shadow-lg border border-gray-100 p-8">
  <!-- Header -->
  <div class="mb-6">
    <h2 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
      <span>➕</span> Create POC
    </h2>
    <p class="text-sm text-gray-600 mt-1">Add a new Point of Contact to the system</p>
  </div>

  <!-- Content -->
  <div class="space-y-4">
    <!-- Form elements here -->
  </div>
</div>
```

**Properties:**
- Background: white (#FFFFFF)
- Border: 1px solid #F3F4F6
- Shadow: `shadow-lg` (0 10px 15px -3px rgba(0,0,0,0.1))
- Border-radius: 16px
- Padding: 32px

---

## ⚠️ Alert / Status Messages

### 1. Success Message
```html
<div class="mb-5 p-4 bg-green-50 border-l-4 border-green-500 rounded-lg 
  flex items-start gap-3 animate-slide-down">
  <span class="text-green-500 text-lg flex-shrink-0">✅</span>
  <div>
    <p class="text-sm font-medium text-green-900">POC created successfully!</p>
    <p class="text-xs text-green-700 mt-0.5">POC001 (Priya) has been added to the system</p>
  </div>
</div>
```

**Features:**
- Soft green background: `#ECFDF5`
- Left border: 4px solid `#10B981`
- Emoji icon for immediate recognition
- Slide-down animation on appearance

### 2. Error Message
```html
<div class="mb-5 p-4 bg-red-50 border-l-4 border-red-500 rounded-lg 
  flex items-start gap-3 animate-slide-down">
  <span class="text-red-500 text-lg flex-shrink-0">❌</span>
  <div>
    <p class="text-sm font-medium text-red-900">Error Creating POC</p>
    <p class="text-xs text-red-700 mt-0.5">Failed to load POCs: status=404 body=...</p>
  </div>
</div>
```

**Features:**
- Soft red background: `#FEF2F2`
- Left border: 4px solid `#EF4444`
- Actionable error message with details
- Dismissible (auto-closes or user can close)

### 3. Warning/Info Message (Optional)
```html
<div class="mb-4 p-4 bg-blue-50 border-l-4 border-blue-500 rounded-lg 
  flex items-start gap-3">
  <span class="text-blue-500 text-lg flex-shrink-0">ℹ️</span>
  <div>
    <p class="text-sm font-medium text-blue-900">Info</p>
    <p class="text-xs text-blue-700 mt-0.5">Your message here</p>
  </div>
</div>
```

---

## 📊 Table Component

### 1. POC Directory Table
```html
<div class="overflow-hidden rounded-lg border border-gray-200">
  <table class="w-full text-sm">
    <thead>
      <tr class="bg-gray-50 border-b border-gray-200">
        <th class="px-4 py-3 text-left font-semibold text-gray-700">Emp ID</th>
        <th class="px-4 py-3 text-left font-semibold text-gray-700">Name</th>
        <th class="px-4 py-3 text-left font-semibold text-gray-700">Email</th>
        <th class="px-4 py-3 text-left font-semibold text-gray-700">Cohorts</th>
      </tr>
    </thead>
    <tbody class="divide-y divide-gray-200">
      <!-- Row 1 (white bg) -->
      <tr class="hover:bg-blue-50 transition-colors bg-white">
        <td class="px-4 py-3">
          <span class="inline-flex items-center gap-2 font-mono text-xs 
            bg-blue-100 text-blue-900 px-2.5 py-1 rounded-full">POC001</span>
        </td>
        <td class="px-4 py-3 font-medium text-gray-900">Priya Mehta</td>
        <td class="px-4 py-3 text-gray-600 text-xs">priya@company.com</td>
        <td class="px-4 py-3">
          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full 
            text-xs font-medium bg-green-100 text-green-800">BATCH-2026-A</span>
        </td>
      </tr>

      <!-- Row 2 (gray bg - alternating) -->
      <tr class="hover:bg-blue-50 transition-colors bg-gray-50">
        <td class="px-4 py-3">
          <span class="inline-flex items-center gap-2 font-mono text-xs 
            bg-blue-100 text-blue-900 px-2.5 py-1 rounded-full">POC002</span>
        </td>
        <td class="px-4 py-3 font-medium text-gray-900">Arun Kumar</td>
        <td class="px-4 py-3 text-gray-600 text-xs">arun@company.com</td>
        <td class="px-4 py-3">
          <div class="flex items-center gap-1">
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full 
              text-xs font-medium bg-green-100 text-green-800">BATCH-2026-B</span>
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full 
              text-xs font-medium bg-green-100 text-green-800">BATCH-2026-C</span>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

**Features:**
- Clean, minimal borders
- Alternating row colors (white & light gray)
- Hover effect: light blue background
- Striped layout for easy reading
- Badges for cohorts (green 100/800)
- Employee ID badges (blue 100/900)

---

## 🎨 Badge Component

### 1. Status Badge
```html
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full 
  text-xs font-medium bg-green-100 text-green-800">BATCH-2026-A</span>
```

### 2. ID Badge
```html
<span class="inline-flex items-center gap-2 font-mono text-xs 
  bg-blue-100 text-blue-900 px-2.5 py-1 rounded-full">POC001</span>
```

**Badge Colors:**
- **Green:** Success/Active `bg-green-100 text-green-800`
- **Blue:** Info/ID `bg-blue-100 text-blue-900`
- **Red:** Inactive/Error `bg-red-100 text-red-900`
- **Gray:** Neutral `bg-gray-100 text-gray-800`

---

## 💫 Loading States

### 1. Loading Spinner
```html
<div class="py-12 flex flex-col items-center justify-center text-center">
  <div class="animate-spin text-4xl mb-3">⏳</div>
  <p class="text-gray-600 font-medium">Loading POCs...</p>
  <p class="text-sm text-gray-500 mt-1">Please wait while we fetch the data</p>
</div>
```

### 2. Empty State
```html
<div class="py-12 flex flex-col items-center justify-center text-center 
  bg-gray-50 rounded-xl border-2 border-dashed border-gray-300">
  <span class="text-5xl mb-3">📭</span>
  <p class="text-gray-600 font-medium">No POCs Found</p>
  <p class="text-sm text-gray-500 mt-1">Create one using the form on the left</p>
</div>
```

---

## 🎯 Layout Examples

### 1. Login Screen Layout
```
┌─────────────────────────────────────────┐
│  bg-gradient-to-br from-slate-50        │
│                                         │
│  ┌──── Card ──────────────────────┐    │
│  │                                 │    │
│  │  [Logo/Icon]                    │    │
│  │  Admin Login                    │    │
│  │  Manage your contacts           │    │
│  │                                 │    │
│  │  [Username Input]               │    │
│  │  [Password Input]               │    │
│  │                                 │    │
│  │  [Login Button]                 │    │
│  │                                 │    │
│  │  Restricted access notice       │    │
│  │                                 │    │
│  └─────────────────────────────────┘    │
│                                         │
└─────────────────────────────────────────┘

Container: 448px max-width
Background: Gradient slate
Card: white, rounded-3xl, shadow-xl
```

### 2. Dashboard Grid Layout
```
┌───────────────────────────────────────────────────────────┐
│  Admin Control Center                          [Logout]   │
│                                                            │
│  ┌─────────────────────┐  ┌──────────────────────────┐   │
│  │  CREATE POC         │  │  POC DIRECTORY           │   │
│  │                     │  │                          │   │
│  │  [Form Fields]      │  │  ┌──────────────────────┐ │  │
│  │  [Form Fields]      │  │  │ Table                │ │  │
│  │  [Form Fields]      │  │  │ ┌──────────────────┐ │ │  │
│  │                     │  │  │ │ Row              │ │ │  │
│  │  [Submit Button]    │  │  │ ├──────────────────┤ │ │  │
│  │                     │  │  │ │ Row              │ │ │  │
│  └─────────────────────┘  │  │ ├──────────────────┤ │ │  │
│                            │  │ │ Row              │ │ │  │
│                            │  │ └──────────────────┘ │ │  │
│                            │  │ Total: N POCs       │ │  │
│                            │  └──────────────────────┘ │  │
│                            └──────────────────────────┘   │
│                                                            │
└───────────────────────────────────────────────────────────┘

Grid: grid-cols-1 lg:grid-cols-2 gap-8
Cards: 32px padding, shadow-lg
```

---

## 🎬 Animations

### 1. Slide-Down (Messages)
```css
@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-down {
  animation: slide-down 0.3s ease-out;
}
```

### 2. Button Hover Lift
```css
/* Applied via Tailwind classes */
button:hover {
  --tw-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  transition: all 200ms;
}
```

### 3. Button Active Press
```css
button:active {
  transform: scale(0.98);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  transition: all 150ms;
}
```

---

## 🌈 Color Reference

### Primary Blue Gradient
```
from-blue-600 (#2563EB)
to-blue-700 (#1D4ED8)

Hover:
from-blue-700 (#1D4ED8)
to-blue-800 (#1E40AF)

Active:
from-blue-800 (#1E40AF)
to-blue-900 (#1E3A8A)
```

### Success Green
```
Background: #ECFDF5 (green-50)
Border:     #10B981 (green-500)
Text:       #115E59 (green-900)
```

### Error Red
```
Background: #FEF2F2 (red-50)
Border:     #EF4444 (red-500)
Text:       #7F1D1D (red-900)
```

### Neutral Grays
```
White:      #FFFFFF
Off-white:  #F9FAFB (gray-50)
Light:      #F3F4F6 (gray-100)
Borders:    #E5E7EB (gray-200)
Secondary:  #6B7280 (gray-500)
Primary:    #1F2937 (gray-900)
```

---

## ✅ Quick Copy-Paste Components

### Button (Primary)
```html
<button class="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 
  hover:to-blue-800 text-white font-semibold py-2.5 rounded-lg transition-all 
  duration-200 shadow-md hover:shadow-lg hover:-translate-y-0.5">
  Submit
</button>
```

### Input
```html
<input type="text" placeholder="Enter text"
  class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
  text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200" />
```

### Success Alert
```html
<div class="p-4 bg-green-50 border-l-4 border-green-500 rounded-lg flex items-start gap-3">
  <span class="text-green-500 text-lg">✅</span>
  <div>
    <p class="text-sm font-medium text-green-900">Success!</p>
    <p class="text-xs text-green-700">Your action completed successfully.</p>
  </div>
</div>
```

### Card Container
```html
<div class="bg-white rounded-2xl shadow-lg border border-gray-100 p-8">
  <!-- Content -->
</div>
```

---

## 📐 Spacing Reference (8px Grid)

| Class | Value | Usage |
|-------|-------|-------|
| `p-2` | 8px | Extra small |
| `p-3` | 12px | Small |
| `p-4` | 16px | Medium |
| `p-6` | 24px | Large |
| `p-8` | 32px | **Extra Large** |

---

## 🎓 Design Principles Summary

✅ **Minimalism** - Clean, uncluttered interface
✅ **Hierarchy** - Clear visual priority and structure
✅ **Consistency** - Unified spacing, colors, typography
✅ **Feedback** - Hover, focus, active states
✅ **Accessibility** - High contrast, keyboard navigation
✅ **Usability** - Intuitive interactions, clear labels
✅ **Professional** - Premium feel, polished details

---

*Component Library v1.0 | Fully Production-Ready*

