import {Pipe, PipeTransform} from "@angular/core";

@Pipe({
  name: 'arrayWithSize'
})
export class ArrayWithSizePipe implements PipeTransform {

  transform(value: number, args?: any): number[] {
    return Array(value).fill(0).map((x,i)=>i);
  }

}
