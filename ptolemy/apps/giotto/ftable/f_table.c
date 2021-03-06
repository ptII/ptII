/* This file was generated by GiottoC: http://www.eecs.berkeley.edu/~fresco/giotto */

#include "f_table.h" 

double GPS;
void driver_GPS_GPS_device_driver_fire () {
  GPS_device_driver_fire(&GPS);
}

double motor;
void driver_motor_motor_device_driver_fire () {
  motor_device_driver_fire(&motor);
}

double global_A_out;
double local_A_out;

void driver_A_out_init_init_function_name_A () {
  init_function_name_A(&global_A_out);
  init_function_name_A(&local_A_out);
}

void driver_A_out_copy_double () {
  copy_double(&local_A_out, &global_A_out);
}

double global_B_out;
double local_B_out;

void driver_B_out_init_init_function_name_B () {
  init_function_name_B(&global_B_out);
  init_function_name_B(&local_B_out);
}

void driver_B_out_copy_double () {
  copy_double(&local_B_out, &global_B_out);
}

double Ain1;
double Ain2;
void task_A () {
  A_fire(&Ain1, &Ain2, &local_A_out);
}

double Bin1;
void task_B () {
  B_fire(&Bin1, &local_B_out);
}

unsigned condition_motor_motor_driver () {
  return motor_guard(&global_B_out);
}

void driver_motor_motor_driver () {
  motor_transferOutputs(&global_B_out, &motor);
}

unsigned condition_A_A_driver () {
  return A_guard(&GPS, &global_A_out);
}

void driver_A_A_driver () {
  A_transferInputs(&GPS, &global_A_out, &Ain1, &Ain2);
}

unsigned condition_B_B_driver () {
  return B_guard(&GPS);
}

void driver_B_B_driver () {
  B_transferInputs(&GPS, &Bin1);
}

trigger_type trigger_table[MAXTRIGGER] = {
  { "giotto_timer", giotto_timer_enable_code, giotto_timer_save_code, giotto_timer_trigger_code }
};

task_type task_table[MAXTASK] = {
  { "A", task_A },
  { "B", task_B }
};

driver_type driver_table[MAXDRIVER] = {
  { "GPS_GPS_device_driver_fire", driver_GPS_GPS_device_driver_fire, 0 },
  { "motor_motor_device_driver_fire", driver_motor_motor_device_driver_fire, 0 },
  { "A_out_init_init_function_name_A", driver_A_out_init_init_function_name_A, 1 },
  { "A_out_copy_double", driver_A_out_copy_double, 1 },
  { "B_out_init_init_function_name_B", driver_B_out_init_init_function_name_B, 2 },
  { "B_out_copy_double", driver_B_out_copy_double, 2 },
  { "motor_motor_driver", driver_motor_motor_driver, 0 },
  { "A_A_driver", driver_A_A_driver, 1 },
  { "B_B_driver", driver_B_B_driver, 2 }
};

condition_type condition_table[MAXCONDITION] = {
  { "motor_motor_driver", condition_motor_motor_driver, 0 },
  { "A_A_driver", condition_A_A_driver, 0 },
  { "B_B_driver", condition_B_B_driver, 0 }
};

