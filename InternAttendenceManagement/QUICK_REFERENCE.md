# Design System Quick Reference
## Copy-Paste Guide for Modern UI Components

This is a quick reference guide for developers to apply the modern design system to other components in the project.

---

## 🎨 Quick Color Reference

### Primary Actions (Blue Gradient)
```tailwind
bg-gradient-to-r from-blue-600 to-blue-700
hover:from-blue-700 hover:to-blue-800
active:from-blue-800 active:to-blue-900
```

### Status Colors
**Success:** `text-green-800 bg-green-50 border-green-500`  
**Error:** `text-red-600 bg-red-50 border-red-300`  
**Warning:** `text-yellow-700 bg-yellow-50 border-yellow-300`  
**Info:** `text-blue-700 bg-blue-50 border-blue-300`  

---

## 📝 Input Components

### Text Input
```html
<input type="text" placeholder="Enter text"
  class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
  text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200" />
```

### Email Input
```html
<input type="email" placeholder="email@example.com"
  class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
  text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200" />
```

### Password Input
```html
<input type="password" placeholder="••••••••"
  class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
  text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200" />
```

### Textarea
```html
<textarea placeholder="Enter your message..."
  class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
  text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200 
  resize-none">
</textarea>
```

### Select Dropdown
```html
<select class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
  text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200">
  <option>Select an option</option>
  <option>Option 1</option>
  <option>Option 2</option>
</select>
```

---

## 🔘 Button Components

### Primary Button (CTA)
```html
<button class="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 
  hover:to-blue-800 active:from-blue-800 active:to-blue-900 text-white font-semibold 
  py-2.5 rounded-lg transition-all duration-200 shadow-md hover:shadow-lg hover:-translate-y-0.5">
  Submit
</button>
```

### Secondary Button
```html
<button class="px-4 py-2.5 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-lg 
  font-medium transition-colors text-sm">
  Action
</button>
```

### Danger Button (Delete/Logout)
```html
<button class="px-4 py-2.5 bg-red-50 text-red-700 hover:bg-red-100 rounded-lg 
  font-medium transition-colors text-sm">
  Delete
</button>
```

### Icon Button
```html
<button class="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg 
  font-medium transition-colors text-sm">
  📖 Learn More
</button>
```

---

## 📋 Alert Components

### Success Alert
```html
<div class="p-4 bg-green-50 border-l-4 border-green-500 rounded-lg 
  flex items-start gap-3 animate-slide-down">
  <span class="text-green-500 text-lg">✅</span>
  <div>
    <p class="text-sm font-medium text-green-900">Success</p>
    <p class="text-xs text-green-700">Your action was completed successfully.</p>
  </div>
</div>
```

### Error Alert
```html
<div class="p-4 bg-red-50 border-l-4 border-red-500 rounded-lg 
  flex items-start gap-3 animate-slide-down">
  <span class="text-red-500 text-lg">❌</span>
  <div>
    <p class="text-sm font-medium text-red-900">Error</p>
    <p class="text-xs text-red-700">Something went wrong. Please try again.</p>
  </div>
</div>
```

### Warning Alert
```html
<div class="p-4 bg-yellow-50 border-l-4 border-yellow-500 rounded-lg 
  flex items-start gap-3">
  <span class="text-yellow-600 text-lg">⚠️</span>
  <div>
    <p class="text-sm font-medium text-yellow-900">Warning</p>
    <p class="text-xs text-yellow-700">Please review this information.</p>
  </div>
</div>
```

### Info Alert
```html
<div class="p-4 bg-blue-50 border-l-4 border-blue-500 rounded-lg 
  flex items-start gap-3">
  <span class="text-blue-500 text-lg">ℹ️</span>
  <div>
    <p class="text-sm font-medium text-blue-900">Info</p>
    <p class="text-xs text-blue-700">Here's some useful information.</p>
  </div>
</div>
```

---

## 🎴 Card Components

### Basic Card
```html
<div class="bg-white rounded-2xl shadow-lg border border-gray-100 p-8">
  <h2 class="text-2xl font-bold text-gray-900 mb-4">Card Title</h2>
  <p class="text-gray-600">Card content here</p>
</div>
```

### Card with Header & Action
```html
<div class="bg-white rounded-2xl shadow-lg border border-gray-100 p-8">
  <div class="flex items-center justify-between mb-6">
    <h2 class="text-2xl font-bold text-gray-900">Section Title</h2>
    <button class="text-sm text-blue-600 hover:text-blue-700">Edit →</button>
  </div>
  <p class="text-gray-600">Card content here</p>
</div>
```

### Minimal Card
```html
<div class="bg-white rounded-xl shadow-md border border-gray-100 p-6">
  <!-- Content -->
</div>
```

---

## 📊 Table Components

### Basic Table
```html
<div class="overflow-hidden rounded-lg border border-gray-200">
  <table class="w-full text-sm">
    <thead>
      <tr class="bg-gray-50 border-b border-gray-200">
        <th class="px-4 py-3 text-left font-semibold text-gray-700">Column 1</th>
        <th class="px-4 py-3 text-left font-semibold text-gray-700">Column 2</th>
      </tr>
    </thead>
    <tbody class="divide-y divide-gray-200">
      <tr class="hover:bg-blue-50 transition-colors">
        <td class="px-4 py-3 text-gray-900">Data 1</td>
        <td class="px-4 py-3 text-gray-600">Data 2</td>
      </tr>
    </tbody>
  </table>
</div>
```

### Table with Alternating Rows
```html
<tbody class="divide-y divide-gray-200">
  @for (item of items; track item.id; let i = $index) {
  <tr class="hover:bg-blue-50 transition-colors" 
    [ngClass]="{'bg-white': i % 2 === 0, 'bg-gray-50': i % 2 === 1}">
    <td class="px-4 py-3">{{ item.name }}</td>
  </tr>
  }
</tbody>
```

---

## 🏷️ Badge Components

### Status Badge
```html
<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium 
  bg-green-100 text-green-800">
  Active
</span>
```

### Info Badge
```html
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium 
  bg-blue-100 text-blue-900">
  PRO001
</span>
```

### Colored Badges
```html
<!-- Green -->
<span class="bg-green-100 text-green-800">Success</span>

<!-- Blue -->
<span class="bg-blue-100 text-blue-900">Info</span>

<!-- Red -->
<span class="bg-red-100 text-red-900">Error</span>

<!-- Yellow -->
<span class="bg-yellow-100 text-yellow-900">Warning</span>

<!-- Gray -->
<span class="bg-gray-100 text-gray-800">Neutral</span>
```

---

## 💬 Dialog/Modal Components

### Modal Container
```html
<div class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
  <div class="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md">
    <h2 class="text-2xl font-bold text-gray-900 mb-4">Modal Title</h2>
    <p class="text-gray-600 mb-6">Modal content here</p>
    
    <div class="flex gap-3">
      <button class="flex-1 px-4 py-2.5 bg-gray-100 text-gray-900 rounded-lg 
        font-medium hover:bg-gray-200 transition-colors">
        Cancel
      </button>
      <button class="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg 
        font-medium hover:bg-blue-700 transition-colors">
        Confirm
      </button>
    </div>
  </div>
</div>
```

---

## 🎯 Form Group (Complete)

### Field with Label & Validation
```html
<div class="space-y-2">
  <label class="block text-sm font-semibold text-gray-900">
    Email Address <span class="text-red-500">*</span>
  </label>
  <input type="email" placeholder="user@example.com"
    class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 
    text-sm focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-200" />
  <p class="text-xs text-gray-500">We'll never share your email.</p>
</div>
```

### Checkbox
```html
<label class="flex items-center gap-2 cursor-pointer">
  <input type="checkbox" class="w-4 h-4 accent-blue-600 rounded" />
  <span class="text-sm text-gray-700">I agree to the terms</span>
</label>
```

### Radio Buttons
```html
<div class="space-y-2">
  <label class="flex items-center gap-2 cursor-pointer">
    <input type="radio" name="option" value="1" class="w-4 h-4 accent-blue-600" />
    <span class="text-sm text-gray-700">Option 1</span>
  </label>
  <label class="flex items-center gap-2 cursor-pointer">
    <input type="radio" name="option" value="2" class="w-4 h-4 accent-blue-600" />
    <span class="text-sm text-gray-700">Option 2</span>
  </label>
</div>
```

---

## 📐 Layout Helpers

### Centered Container
```html
<div class="max-w-6xl mx-auto px-4">
  <!-- Content -->
</div>
```

### Two-Column Grid
```html
<div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
  <div>Column 1</div>
  <div>Column 2</div>
</div>
```

### Three-Column Grid
```html
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
  <!-- Items -->
</div>
```

### Flex Row with Gap
```html
<div class="flex items-center gap-4">
  <!-- Items -->
</div>
```

### Flex Column with Gap
```html
<div class="flex flex-col gap-4">
  <!-- Items -->
</div>
```

---

## ✨ Global Animations

### Add to your component's CSS
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

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fade-in 0.3s ease-out;
}

@keyframes spin-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.animate-spin-slow {
  animation: spin-slow 2s linear infinite;
}
```

---

## 📱 Responsive Classes

### Hide/Show by Breakpoint
```tailwind
hidden              /* All sizes */
block lg:hidden     /* Show by default, hide on lg+ */
hidden lg:block     /* Hide by default, show on lg+ */
```

### Responsive Padding
```tailwind
p-4 lg:p-8          /* 16px mobile, 32px desktop */
px-4 md:px-6 lg:px-8
```

### Responsive Grid
```tailwind
grid-cols-1         /* Mobile */
md:grid-cols-2      /* Tablet */
lg:grid-cols-3      /* Desktop */
```

---

## 🔗 Typography Shortcuts

### Heading Styles
```html
<!-- H1 -->
<h1 class="text-4xl font-bold text-gray-900 tracking-tight">Title</h1>

<!-- H2 -->
<h2 class="text-2xl font-bold text-gray-900">Subtitle</h2>

<!-- H3 -->
<h3 class="text-lg font-semibold text-gray-900">Section</h3>

<!-- Body -->
<p class="text-base text-gray-600">Paragraph text</p>

<!-- Small -->
<p class="text-sm text-gray-500">Small text</p>

<!-- Extra Small -->
<p class="text-xs text-gray-400">Extra small</p>
```

---

## 🎯 Accessibility Essentials

### Focus States
```tailwind
focus:outline-none
focus:ring-2
focus:ring-blue-200
focus:border-blue-500
```

### Skip Link
```html
<a href="#main" class="sr-only focus:not-sr-only">
  Skip to main content
</a>
```

### ARIA Labels
```html
<button aria-label="Close modal">×</button>
<nav aria-label="Main navigation">
  <!-- Navigation items -->
</nav>
```

---

## 📊 Spacing Reference

| Size | Pixels | Tailwind |
|------|--------|----------|
| xs | 4px | `p-1`, `m-1` |
| sm | 8px | `p-2`, `m-2` |
| md | 12px | `p-3`, `m-3` |
| lg | 16px | `p-4`, `m-4` |
| xl | 24px | `p-6`, `m-6` |
| 2xl | 32px | `p-8`, `m-8` |
| 3xl | 48px | `p-12`, `m-12` |

---

## 🎨 Color Reference

```tailwind
/* Text Colors */
text-gray-900      /* Primary text */
text-gray-600      /* Secondary text */
text-gray-500      /* Tertiary text */
text-gray-400      /* Disabled text */

/* Background Colors */
bg-white           /* Main background */
bg-gray-50         /* Secondary background */
bg-gray-100        /* Tertiary background */

/* Border Colors */
border-gray-200    /* Light border */
border-gray-300    /* Medium border */
border-gray-400    /* Dark border */

/* Status Colors */
text-green-800     /* Success text */
text-red-600       /* Error text */
text-yellow-700    /* Warning text */
text-blue-700      /* Info text */
```

---

## 💡 Tips & Tricks

**Pro Tips:**
1. Use `space-y-4` instead of individual margins for consistent vertical spacing
2. Use `divide-y` for lists instead of individual borders
3. Use `group` and `group-hover:` for hover effects on parent/child
4. Use `sr-only` for screen-reader-only content
5. Use `truncate` for long text instead of `overflow-hidden`
6. Use `transition-all duration-200` for smooth interactions
7. Use `cursor-pointer` on clickable elements
8. Use `disabled:opacity-50` for disabled states

---

**Version 1.0** | Quick Reference Guide  
Apply these patterns across your entire project for consistency.

