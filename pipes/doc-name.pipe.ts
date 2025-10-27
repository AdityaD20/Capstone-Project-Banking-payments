import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'docName',
  standalone: true
})
export class DocNamePipe implements PipeTransform {

  // This map contains the keys used during upload and their friendly display names
  private readonly nameMap: { [key: string]: string } = {
    'registration_certificate': 'Registration Certificate',
    'tax_certificate': 'Tax Certificate',
    'bank_passbook': 'Bank Passbook / Statement',
    'initial_balance_cheque': 'Cheque for Initial Balance',
    'aadhar_card': 'Aadhar Card',
    'pan_card': 'PAN Card',
    'cancelled_cheque': 'Cancelled Cheque'
  };

  transform(value: string): string {
    if (!value) {
      return 'Unnamed Document';
    }

    // Find the key that the filename starts with
    for (const key in this.nameMap) {
      if (value.startsWith(key)) {
        return this.nameMap[key];
      }
    }

    // If no key matches, return a formatted version of the original string
    return value.split('_').join(' ').replace(/\.[^/.]+$/, "");
  }
}