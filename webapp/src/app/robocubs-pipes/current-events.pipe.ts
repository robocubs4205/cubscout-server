import {Pipe, PipeTransform} from "@angular/core";
import {Event} from "../event";
@Pipe({name:"currentEvents",pure:false})
export class CurrentEventsPipe implements PipeTransform{
  transform(value: any, ...args: any[]): any {
    return (<Event[]>value).filter(event=>(event.startDate==null||event.startDate<=new Date())&&(event.endDate==null||event.endDate>=new Date()));
  }

}
