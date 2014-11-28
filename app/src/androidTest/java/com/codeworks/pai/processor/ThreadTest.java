package com.codeworks.pai.processor;

import junit.framework.TestCase;

public class ThreadTest extends TestCase {

	public void testRunnableThread() throws InterruptedException {
		Thread t = new Thread(new MyRunnable());
		t.start();
		for (int i = 0; i < 10; i++) {
			Thread.sleep(200);
			System.out.println("Notify");
			synchronized (lock) {
				lock.notify();
			}
			Thread.sleep(Updater.DELAY);
		}
		t.interrupt();
	}

	class MyRunnable implements Runnable {

		static final long DELAY = 100000L;
		boolean running = false;

		public boolean isRunning() {
			return running;
		}

		@Override
		public void run() {
			running = true;
			while (running) {
				try {
					System.out.println("Updater Running wait (" + DELAY + ")");
					long start = System.currentTimeMillis();
					synchronized (lock) {
						lock.wait(DELAY);
					}
					System.out.println("Return from wait in ms=" + (System.currentTimeMillis() - start));
				} catch (InterruptedException e) {
					System.out.println("Service has been interrupted");
					running = false;
				}
			}

		}
	}

	private static final class Lock {
	}

	private final Object lock = new Lock();

	public void testThread() throws InterruptedException {
		Updater t = new Updater();
		t.start();
		for (int i = 0; i < 10; i++) {
			Thread.sleep(200);
			System.out.println("Notify");
			synchronized (lock) {
				lock.notify();
			}
			Thread.sleep(Updater.DELAY);
		}
		t.interrupt();
		t = null;
	}

	class Updater extends Thread {
		public Updater() {
			super("Updater");
		}

		static final long DELAY = 1000L;
		boolean running = false;
		boolean restart = false;

		public boolean isRunning() {
			return running;
		}

		public void restart() {
			restart = true;
			interrupt();
		}

		@Override
		public void run() {
			running = true;

			while (running) {
				try {
					System.out.println("Updater Running wait (" + DELAY + ")");
					long start = System.currentTimeMillis();
					synchronized (lock) {
						lock.wait(DELAY);
					}
					System.out.println("Return from wait in ms=" + (System.currentTimeMillis() - start));
				} catch (InterruptedException e) {
					System.out.println("Service has been interrupted");
					running = false;
				}
			}
		}
	}

}
