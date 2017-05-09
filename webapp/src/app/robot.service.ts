import { Injectable } from '@angular/core';
import {Robot} from "./robot";
import {Match} from "./match";
import {Http, RequestOptions, Headers, Response} from "@angular/http";
import {Observable} from "rxjs";
import {Link} from "./link";
import {Result} from "./result";

@Injectable()
export class RobotService {

  getRobot(result:Result):Observable<Robot>{
    let link: Link = result.links.find(link => link.rel == "robot");
    if (link == null) return Observable.throw("result does not have link with rel \"robot\"");
    return this.http.get(link.href,new RequestOptions({
      headers: new Headers([
        {'accept': 'application/vnd.robocubs-v1+json'}
      ])
    })).map(res=>res.json()).catch(error => {
      let errMsg: string;
      if (error instanceof Response) {
        const body = error.json() || '';
        const err = body.error || JSON.stringify(body);
        errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        errMsg += JSON.stringify(error);
      } else {
        errMsg = error.message ? error.message : error.toString();
      }
      console.error("failed to get robot");
      console.error(errMsg);
      return Observable.throw(errMsg);
    });
  }

  constructor(private http:Http) { }

}
