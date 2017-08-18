import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'abbreviate'
})
export class AbbreviatePipe implements PipeTransform {

  transform(value: String, args?: any): String {
    const max: number = (args && args.length === 1) ? parseInt(args[0], 10) : 100;
    return value && value.length > max ? value.substr(0, max) + '...' : value;
  }

}
