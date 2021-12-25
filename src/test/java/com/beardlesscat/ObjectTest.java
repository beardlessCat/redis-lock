package com.beardlesscat;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.UUID;
@Slf4j
public class ObjectTest {
    @Test
    public void test(){
        new Thread(()->{
            Student student = new Student();
            log.info("线程：{}学生信息为：{}",Thread.currentThread().getName(),student.toString());
        },"thread1").start();

        new Thread(()->{
            Student student = new Student();
            log.info("线程：{}学生信息为：{}",Thread.currentThread().getName(),student.toString());
        },"thread2").start();

        new Thread(()->{
            Student student = new Student();
            log.info("线程：{}学生信息为：{}",Thread.currentThread().getName(),student.toString());
        },"thread3").start();

        Student student = new Student();
        log.info("线程：{}学生信息为：{}",Thread.currentThread().getName(),student.toString());
    }
    static class Student{
        private static String uuid ;

        public Student() {
            this.uuid = UUID.randomUUID().toString();
        }

        @Override
        public String toString() {
            return uuid;
        }
    }
}
