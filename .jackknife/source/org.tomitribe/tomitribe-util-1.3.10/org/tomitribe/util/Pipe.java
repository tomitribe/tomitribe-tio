package org.tomitribe.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public final class Pipe implements Runnable {
   private final InputStream in;
   private final OutputStream out;

   public Pipe(InputStream in, OutputStream out) {
      this.in = in;
      this.out = out;
   }

   public static void pipe(Process process) {
      pipe(process.getInputStream(), System.out);
      pipe(process.getErrorStream(), System.err);
   }

   public static Future<Pipe> pipe(InputStream in, OutputStream out) {
      Pipe target = new Pipe(in, out);
      FutureTask<Pipe> task = new FutureTask(target, target);
      Thread thread = new Thread(task);
      thread.setDaemon(true);
      thread.start();
      return task;
   }

   public void run() {
      try {
         int i = -1;
         byte[] buf = new byte[1024];

         while ((i = this.in.read(buf)) != -1) {
            this.out.write(buf, 0, i);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }
}
