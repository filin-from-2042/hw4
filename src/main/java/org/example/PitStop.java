package org.example;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

@Log4j2
public class PitStop extends Thread {

    PitWorker[] workers = new PitWorker[4];
    // Semaphore с permits 1 был выбран в учебных целях, можно было бы обойтись обычной блокировкой
    private final Semaphore pitlineSemaphore = new Semaphore(1);

    private final CyclicBarrier completeWorkBarier;
    private final CyclicBarrier startWorkBarier;
    private F1Cars currentCar;

    public PitStop() {
        startWorkBarier = new CyclicBarrier(workers.length + 1);
        completeWorkBarier = new CyclicBarrier(workers.length + 1);

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new PitWorker(i, this);
            workers[i].start();
        }
    }

    public void pitline(F1Cars f1Cars) {
        try {
            pitlineSemaphore.acquire();
            log.info("авто под номером {} заехало на питстоп {}", f1Cars.getId(), this.getName());
            currentCar = f1Cars;

            startWorkBarier.await();
            completeWorkBarier.await();
        } catch (InterruptedException | BrokenBarrierException interruptedException) {
            //
        } finally {
            log.info("авто под номером {} выехало с питстопа {}", f1Cars.getId(), this.getName());
            pitlineSemaphore.release();
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            //синхронизируем поступающие болиды и работников питстопа при необходимости
        }
    }

    public F1Cars getCar() {
        try {
            startWorkBarier.await();
        } catch (InterruptedException | BrokenBarrierException exception) {
            throw new RuntimeException(exception);
        }

        return currentCar;
    }

    public void notifyComplete() {
        try {
            completeWorkBarier.await();
        } catch (InterruptedException | BrokenBarrierException exception) {
            throw new RuntimeException(exception);
        }
    }
}
