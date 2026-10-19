
# Task 2

why does making only `increment()` synchronized but not get() still leave a bug?
- Because if we don't make `get()` synchronized they won't get expected value. If `increment()` is running and we do unsynchronized `get()` it will not wait for `increment()` and just give old value.

# Task 3

Experiment: change `notifyAll()` to `notify()` and the while to an if. Describe in README.md what breaks and why (hint: run it 20 times in a loop).
- `while` means check again before continuing. A thread might wake up, but the box may still be full or empty. It waits again instead of doing the wrong thing. 
- `if` checks only once, so it might continue when it shouldn’t.
- `notify()`  wakes one waiting thread, chosen at random. It might wake the wrong kind of thread.
- `notifyAll()`  wakes everyone waiting. They take turns checking the box; only the threads that can proceed will continue.
- So `while + notifyAll()` is safer for this type of buffer.

Explains why `while + notifyAll()` is required.
- `while + notifyAll()` is required because since inside the while there a wait()
- `notifyAll()` is telling all the while loop that using the `wait()` to check it condition again.
- Without `notifyAll()` the while loop will stuck there a the `wait()` won't release.

# Task 4
| Fixed pool | Virtual threads |
|---|---|
| Only 20 tasks run concurrently | Potentially all 500 tasks run concurrently |
| Uses 20 platform threads | Uses lightweight virtual threads |
| Remaining tasks wait in a queue | Many tasks can wait efficiently |
| Approximately 5 seconds | Often close to 200–500 ms |
| Good when limiting concurrency is important | Good for many blocking I/O tasks |