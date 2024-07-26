package org.example;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Log4j2
public class Race {

    @Getter
    private final long distance;
    private long startTime;

    private final CountDownLatch startCdl;
    private final CountDownLatch finishCdl;

    private final List<F1Cars> participantCars = new java.util.ArrayList<>();

    private final List<Team> teams = new java.util.ArrayList<>();

    public Race(long distance, Team[] participantCars) {
        this.distance = distance;
        teams.addAll(List.of(participantCars));
        int allCarsCount = teams.stream().flatMap(team -> Stream.of(team.getCarsCount())).reduce(0, Integer::sum);
        startCdl = new CountDownLatch(allCarsCount);
        finishCdl = new CountDownLatch(allCarsCount);
    }

    /**
     * Запускаем гонку
     */
    public void start() {
        for (Team team : teams) {
            team.prepareRace(this);
        }

        try {
            startCdl.await();
            log.info("Старт!");
            startTime = System.nanoTime();

            finishCdl.await();
        } catch (InterruptedException interruptedException){
            throw new RuntimeException(interruptedException);
        }
    }


    //Регистрируем участников гонки
    public synchronized void register(F1Cars participantCar) {
        participantCars.add(participantCar);
    }


    public void start(F1Cars f1Cars) {
        try {
            this.register(f1Cars);
            startCdl.countDown();
            startCdl.await();
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
        }
        //фиксация времени старта
    }

    public long finish(F1Cars participant) {
        log.info("Участник {} финишировал", participant.getId());
        long raceTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        finishCdl.countDown();
        //фиксация времени финиша
        return raceTime; //длительность гонки у данного участника
    }

    public void printResults() {
        participantCars.sort(F1Cars::compareTo);
        log.info("Результат гонки:");
        int position = 0;
        for (F1Cars participant : participantCars) {
            log.info("Позиция: {} участник: {} время: {}", position++, participant.getId(), participant.getTime());
        }
    }
}
