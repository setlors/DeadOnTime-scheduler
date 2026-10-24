<h1 align="center">💀⏰ DeadOnTime</h1>

<p align="center">
  <b>Beat every deadline. Arrive dead on time.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-orange?logo=openjdk&logoColor=white" alt="Java 17+">
  <img src="https://img.shields.io/badge/build-Maven-blue?logo=apachemaven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/tests-JUnit%205-green?logo=junit5&logoColor=white" alt="JUnit 5">
</p>

---

**DeadOnTime** is a Java command-line app that builds a day-by-day schedule from your tasks, deadlines, and priorities.

It ships with two scheduling engines, a fast **greedy** algorithm and an evolving **genetic** algorithm, so you can compare them on the same data and see which one plans your week better.

> Built for students and freelancers who juggle too many deadlines and too few hours.

---

## Features

| | Feature | Description |
|---|---|---|
| 📝 | **Smart task input** | Add tasks with a time estimate, deadline, and priority (1 to 5) |
| 🗓️ | **Flexible day setup** | Define working hours, daily workload limit, breaks, and busy slots (classes, meetings, lunch) |
| ✂️ | **Splittable tasks** | Big tasks can be broken into smaller chunks automatically |
| ⚡ | **Greedy scheduler** | Fast and predictable |
| 🧬 | **Genetic scheduler** | Slower, but searches for a better order of tasks |
| 📊 | **Schedule scoring** | Every schedule gets a number, so algorithms can be compared fairly |
| 🚫 | **Honest reporting** | Tasks that cannot fit before their deadline are listed, not hidden |
| 💾 | **JSON storage** | Tasks and settings are saved to JSON |
| 📤 | **Export** | Save the final schedule to `.ics` (Google Calendar, Apple Calendar) or Markdown |

---

## How it works

### 1️⃣ Free slots

DeadOnTime takes your working hours, subtracts your busy slots, and gets a list of free time windows. Tasks can only be placed inside these windows.

### 2️⃣ Placing tasks

A `Placer` takes tasks **in a given order** and puts each one into the first free window where it fits.

- Splittable tasks are cut into chunks (never smaller than their minimum chunk)
- A task that cannot be finished before its deadline goes to the `unscheduled` list

### 3️⃣ Greedy algorithm ⚡

> At every step, take the option that looks best right now and never go back.

1. Calculate urgency for each task: `urgency = priority / hoursUntilDeadline`
2. Sort tasks from most to least urgent
3. Place them in that order

**Pros:** fast and simple
**Cons:** it can paint itself into a corner, because one early decision may block two smaller tasks later

### 4️⃣ Genetic algorithm 🧬

> Inspired by evolution. Instead of building one schedule, it evolves many.

**Pros:** often finds better schedules on crowded weeks
**Cons:** slower and slightly random

### Greedy vs Genetic

| | ⚡ Greedy | 🧬 Genetic |
|---|---|---|
| **Speed** | Very fast | Slower |
| **Result** | Good and predictable | Often better on crowded weeks |
| **Randomness** | None | Yes |
| **Best for** | Quick daily planning | Busy weeks with tight deadlines |
