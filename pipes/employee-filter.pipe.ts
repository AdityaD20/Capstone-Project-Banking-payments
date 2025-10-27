import { Pipe, PipeTransform } from '@angular/core';
import { Employee } from '../core/services/employee.service';

@Pipe({
  name: 'employeeFilter',
  standalone: true
})
export class EmployeeFilterPipe implements PipeTransform {

  transform(employees: Employee[] | null, searchText: string): Employee[] {
    if (!employees) {
      return [];
    }
    if (!searchText) {
      return employees;
    }

    searchText = searchText.toLowerCase();

    return employees.filter(employee => {
      const fullName = `${employee.firstName} ${employee.lastName}`.toLowerCase();
      return fullName.includes(searchText);
    });
  }
}